package ir.ac.ap.civilization.model.game;

import ir.ac.ap.civilization.model.building.BuildingType;
import ir.ac.ap.civilization.model.building.ResourceBuilding;
import ir.ac.ap.civilization.model.building.TownHall;
import ir.ac.ap.civilization.model.map.HexCoordinate;
import ir.ac.ap.civilization.model.map.HexTile;
import ir.ac.ap.civilization.model.map.NaturalResourceType;
import ir.ac.ap.civilization.model.resource.ResourceStockpile;
import ir.ac.ap.civilization.model.resource.ResourceType;
import ir.ac.ap.civilization.model.technology.TechnologyType;
import ir.ac.ap.civilization.model.unit.Builder;
import ir.ac.ap.civilization.model.unit.Unit;
import ir.ac.ap.civilization.model.unit.Worker;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;

public final class ResourceBuildingService {

    public static final int STATION_ACTION_POINT_COST = 1;

    private final GameState gameState;

    public ResourceBuildingService(GameState gameState) {
        this.gameState = Objects.requireNonNull(
                gameState,
                "Game state cannot be null"
        );
    }

    public boolean canConstruct(
            int builderId,
            BuildingType buildingType
    ) {
        Objects.requireNonNull(
                buildingType,
                "Building type cannot be null"
        );

        try {
            validateConstruction(builderId, buildingType);
            return true;
        } catch (
                IllegalArgumentException |
                IllegalStateException exception
        ) {
            return false;
        }
    }

    public ResourceBuilding construct(
            int builderId,
            BuildingType buildingType
    ) {
        Objects.requireNonNull(
                buildingType,
                "Building type cannot be null"
        );

        ConstructionContext context =
                validateConstruction(
                        builderId,
                        buildingType
                );

        ResourceStockpile stockpile =
                gameState.getTownHall().getStockpile();

        stockpile.spend(
                buildingType.getConstructionCost()
        );

        context.builder.spendActionPoints(
                buildingType
                        .getConstructionActionPointCost()
        );

        context.builder.useCharge();

        ResourceBuilding building =
                gameState.addResourceBuilding(
                        buildingType,
                        context.tile.getCoordinate(),
                        context.naturalResourceType
                );

        if (!context.builder.hasCharges()) {
            gameState.removeUnit(context.builder);
        }

        gameState.refreshVisibility();
        return building;
    }

    public boolean canStationWorker(int workerId) {
        try {
            validateWorkerStationing(workerId);
            return true;
        } catch (
                IllegalArgumentException |
                IllegalStateException exception
        ) {
            return false;
        }
    }

    public void stationWorker(int workerId) {
        WorkerStationContext context =
                validateWorkerStationing(workerId);

        context.worker.spendActionPoints(
                STATION_ACTION_POINT_COST
        );

        context.building.stationWorker(workerId);
        context.worker.stationAt(context.building.getId());
    }

    public boolean canUnstationWorker(int workerId) {
        Optional<Unit> possibleUnit =
                gameState.getUnitById(workerId);

        if (!possibleUnit.isPresent() ||
                !(possibleUnit.get() instanceof Worker)) {
            return false;
        }

        Worker worker = (Worker) possibleUnit.get();
        return worker.isStationed();
    }

    public void unstationWorker(int workerId) {
        Unit unit = getRequiredUnit(workerId);

        if (!(unit instanceof Worker)) {
            throw new IllegalArgumentException(
                    "Only a Worker can leave a building"
            );
        }

        Worker worker = (Worker) unit;

        if (!worker.isStationed()) {
            throw new IllegalStateException(
                    "Worker is not stationed"
            );
        }

        int buildingId = worker.getStationedBuildingId();

        ResourceBuilding building = gameState
                .getResourceBuildingById(buildingId)
                .orElseThrow(
                        () -> new IllegalStateException(
                                "Worker building does not exist"
                        )
                );

        building.removeWorker(workerId);
        worker.leaveBuilding();
    }

    public int produceAllResources() {
        int totalProduced = 0;

        for (ResourceBuilding building :
                gameState.getResourceBuildings()) {
            totalProduced = Math.addExact(
                    totalProduced,
                    produceFromBuilding(building)
            );
        }

        return totalProduced;
    }

    private int produceFromBuilding(
            ResourceBuilding building
    ) {
        if (building.getStationedWorkerCount() == 0) {
            return 0;
        }

        HexTile tile = gameState.getGameMap()
                .getTile(building.getPosition())
                .orElseThrow(
                        () -> new IllegalStateException(
                                "Building is outside the game map"
                        )
                );

        NaturalResourceType naturalResourceType =
                building.getNaturalResourceType();

        if (!tile.hasAvailableNaturalResource(
                naturalResourceType
        )) {
            releaseAllWorkers(building);
            return 0;
        }

        int requestedProduction = Math.multiplyExact(
                building.getBuildingType()
                        .getBaseProductionPerWorker(),
                building.getStationedWorkerCount()
        );

        if (isMiningBuilding(building.getBuildingType())) {
            requestedProduction = (int) Math.floor(
                    requestedProduction *
                            gameState.getTownHall()
                                    .getMiningProductionMultiplier()
            );
        }

        int extractedAmount = tile.extractNaturalResource(
                naturalResourceType,
                requestedProduction
        );

        ResourceType producedResource =
                building.getBuildingType()
                        .getProducedResource();

        gameState.getTownHall()
                .getStockpile()
                .add(producedResource, extractedAmount);

        if (tile.isNaturalResourceDepleted(
                naturalResourceType
        )) {
            releaseAllWorkers(building);
        }

        return extractedAmount;
    }

    private ConstructionContext validateConstruction(
            int builderId,
            BuildingType buildingType
    ) {
        Unit unit = getRequiredUnit(builderId);

        if (!(unit instanceof Builder)) {
            throw new IllegalArgumentException(
                    "Only a Builder can construct buildings"
            );
        }

        Builder builder = (Builder) unit;
        HexCoordinate position = builder.getPosition();

        HexTile tile = gameState.getGameMap()
                .getTile(position)
                .orElseThrow(
                        () -> new IllegalStateException(
                                "Builder is outside the game map"
                        )
                );

        if (!tile.isInsidePlayerTerritory()) {
            throw new IllegalStateException(
                    "Buildings must be constructed inside player territory"
            );
        }

        if (!tile.isConstructionAllowed()) {
            throw new IllegalStateException(
                    "Construction is not allowed on this terrain"
            );
        }

        if (position.equals(
                gameState.getTownHallCoordinate()
        )) {
            throw new IllegalStateException(
                    "Cannot construct over the Town Hall"
            );
        }

        if (gameState.getResourceBuildingAt(position)
                .isPresent()) {
            throw new IllegalStateException(
                    "This hex already contains a building"
            );
        }

        NaturalResourceType naturalResourceType =
                findCompatibleResource(tile, buildingType)
                        .orElseThrow(
                                () -> new IllegalStateException(
                                        "Hex does not contain the required available resource"
                                )
                        );

        validateTechnology(buildingType);

        if (!builder.hasCharges()) {
            throw new IllegalStateException(
                    "Builder has no remaining charges"
            );
        }

        int actionPointCost = buildingType
                .getConstructionActionPointCost();

        if (!builder.canSpendActionPoints(actionPointCost)) {
            throw new IllegalStateException(
                    "Builder does not have enough action points"
            );
        }

        TownHall townHall = gameState.getTownHall();

        if (!townHall.getStockpile().canAfford(
                buildingType.getConstructionCost()
        )) {
            throw new IllegalStateException(
                    "Not enough resources to construct building"
            );
        }

        return new ConstructionContext(
                builder,
                tile,
                naturalResourceType
        );
    }

    private WorkerStationContext validateWorkerStationing(
            int workerId
    ) {
        Unit unit = getRequiredUnit(workerId);

        if (!(unit instanceof Worker)) {
            throw new IllegalArgumentException(
                    "Only a Worker can be stationed"
            );
        }

        Worker worker = (Worker) unit;

        if (worker.isStationed()) {
            throw new IllegalStateException(
                    "Worker is already stationed"
            );
        }

        ResourceBuilding building = gameState
                .getResourceBuildingAt(worker.getPosition())
                .orElseThrow(
                        () -> new IllegalStateException(
                                "Worker must stand on a resource building"
                        )
                );

        if (!building.hasWorkerCapacity()) {
            throw new IllegalStateException(
                    "Building worker capacity is full"
            );
        }

        HexTile tile = gameState.getGameMap()
                .getTile(building.getPosition())
                .orElseThrow(
                        () -> new IllegalStateException(
                                "Building is outside the game map"
                        )
                );

        if (!tile.hasAvailableNaturalResource(
                building.getNaturalResourceType()
        )) {
            throw new IllegalStateException(
                    "The natural resource deposit is depleted"
            );
        }

        if (!worker.canSpendActionPoints(
                STATION_ACTION_POINT_COST
        )) {
            throw new IllegalStateException(
                    "Worker does not have enough action points"
            );
        }

        return new WorkerStationContext(worker, building);
    }

    private Unit getRequiredUnit(int unitId) {
        return gameState.getUnitById(unitId)
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "No unit exists with id: " +
                                        unitId
                        )
                );
    }

    private Optional<NaturalResourceType>
    findCompatibleResource(
            HexTile tile,
            BuildingType buildingType
    ) {
        for (NaturalResourceType naturalResourceType :
                buildingType
                        .getCompatibleNaturalResources()) {
            if (tile.hasAvailableNaturalResource(
                    naturalResourceType
            )) {
                return Optional.of(naturalResourceType);
            }
        }

        return Optional.empty();
    }

    private void validateTechnology(
            BuildingType buildingType
    ) {
        Optional<TechnologyType> requiredTechnology =
                buildingType.getRequiredTechnology();

        if (requiredTechnology.isPresent() &&
                !gameState.getTownHall().hasTechnology(
                        requiredTechnology.get()
                )) {
            throw new IllegalStateException(
                    requiredTechnology.get().getDisplayName() +
                            " technology is required"
            );
        }
    }

    private boolean isMiningBuilding(
            BuildingType buildingType
    ) {
        return buildingType == BuildingType.STONE_MINE ||
                buildingType == BuildingType.IRON_MINE;
    }

    private void releaseAllWorkers(
            ResourceBuilding building
    ) {
        for (Integer workerId : new ArrayList<>(
                building.getStationedWorkerIds()
        )) {
            Unit unit = gameState.getUnitById(workerId)
                    .orElse(null);

            if (unit instanceof Worker) {
                Worker worker = (Worker) unit;

                if (worker.isStationed() &&
                        Integer.valueOf(building.getId()).equals(
                                worker.getStationedBuildingId()
                        )) {
                    worker.leaveBuilding();
                }
            }
        }

        building.removeAllWorkers();
    }

    private static final class ConstructionContext {

        private final Builder builder;
        private final HexTile tile;
        private final NaturalResourceType naturalResourceType;

        private ConstructionContext(
                Builder builder,
                HexTile tile,
                NaturalResourceType naturalResourceType
        ) {
            this.builder = builder;
            this.tile = tile;
            this.naturalResourceType = naturalResourceType;
        }
    }

    private static final class WorkerStationContext {

        private final Worker worker;
        private final ResourceBuilding building;

        private WorkerStationContext(
                Worker worker,
                ResourceBuilding building
        ) {
            this.worker = worker;
            this.building = building;
        }
    }
}
