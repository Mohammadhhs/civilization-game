package ir.ac.ap.civilization.model.game;

import ir.ac.ap.civilization.model.building.MaintainedBuilding;
import ir.ac.ap.civilization.model.building.ResourceBuilding;
import ir.ac.ap.civilization.model.map.HexTile;
import ir.ac.ap.civilization.model.resource.ResourceType;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

public final class EconomyForecastService {

    private final GameState gameState;

    public EconomyForecastService(GameState gameState) {
        this.gameState = Objects.requireNonNull(
                gameState,
                "Game state cannot be null"
        );
    }

    public ResourceFlowForecast calculate() {
        EnumMap<ResourceType, Integer> changes =
                new EnumMap<>(ResourceType.class);

        for (ResourceType resourceType :
                ResourceType.values()) {
            changes.put(
                    resourceType,
                    gameState.getTownHall()
                            .getBaseProduction(resourceType)
            );
        }

        addResourceBuildingProduction(changes);
        subtractUnitFood(changes);
        subtractBuildingUpkeep(changes);

        return new ResourceFlowForecast(changes);
    }

    private void addResourceBuildingProduction(
            Map<ResourceType, Integer> changes
    ) {
        for (ResourceBuilding building :
                gameState.getResourceBuildings()) {
            int workerCount =
                    building.getStationedWorkerCount();

            if (workerCount == 0) {
                continue;
            }

            HexTile tile = gameState.getGameMap()
                    .getTile(building.getPosition())
                    .orElse(null);

            if (tile == null ||
                    !tile.hasAvailableNaturalResource(
                            building.getNaturalResourceType()
                    )) {
                continue;
            }

            int production = Math.multiplyExact(
                    building.getBuildingType()
                            .getBaseProductionPerWorker(),
                    workerCount
            );

            if (building.getBuildingType()
                    .getProducedResource() == ResourceType.STONE ||
                    building.getBuildingType()
                            .getProducedResource() == ResourceType.IRON) {
                production = (int) Math.floor(
                        production *
                                gameState.getTownHall()
                                        .getMiningProductionMultiplier()
                );
            }

            production = Math.min(
                    production,
                    tile.getRemainingResourceAmount(
                            building.getNaturalResourceType()
                    )
            );

            add(
                    changes,
                    building.getBuildingType()
                            .getProducedResource(),
                    production
            );
        }
    }

    private void subtractUnitFood(
            Map<ResourceType, Integer> changes
    ) {
        add(
                changes,
                ResourceType.FOOD,
                -Math.multiplyExact(
                        gameState.getActiveUnitCount(),
                        EconomyService.FOOD_UPKEEP_PER_UNIT
                )
        );
    }

    private void subtractBuildingUpkeep(
            Map<ResourceType, Integer> changes
    ) {
        for (MaintainedBuilding building :
                gameState.getMaintainedBuildings()) {
            for (Map.Entry<ResourceType, Integer> entry :
                    building.getUpkeepCost().entrySet()) {
                add(
                        changes,
                        entry.getKey(),
                        -entry.getValue()
                );
            }
        }
    }

    private void add(
            Map<ResourceType, Integer> changes,
            ResourceType resourceType,
            int amount
    ) {
        changes.put(
                resourceType,
                Math.addExact(
                        changes.get(resourceType),
                        amount
                )
        );
    }
}
