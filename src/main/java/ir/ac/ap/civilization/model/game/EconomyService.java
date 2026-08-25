package ir.ac.ap.civilization.model.game;

import ir.ac.ap.civilization.model.building.ResourceBuilding;
import ir.ac.ap.civilization.model.resource.ResourceStockpile;
import ir.ac.ap.civilization.model.resource.ResourceType;
import ir.ac.ap.civilization.model.unit.Unit;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class EconomyService {

    public static final int FOOD_UPKEEP_PER_UNIT = 2;
    public static final int STARVATION_ACTION_POINT_PENALTY = 2;

    private final GameState gameState;

    public EconomyService(GameState gameState) {
        this.gameState = Objects.requireNonNull(
                gameState,
                "Game state cannot be null"
        );
    }

    public void applyEndTurnCosts() {
        FoodPayment foodPayment = consumeUnitFood();
        BuildingUpkeepResult buildingUpkeep =
                payBuildingUpkeep();

        gameState.getEconomyStatus().update(
                foodPayment.starving,
                foodPayment.requiredFood,
                foodPayment.paidFood,
                buildingUpkeep.buildingsAtRisk,
                buildingUpkeep.destroyedBuildings
        );
    }

    public int calculateRequiredUnitFood() {
        return Math.multiplyExact(
                gameState.getUnits().size(),
                FOOD_UPKEEP_PER_UNIT
        );
    }

    private FoodPayment consumeUnitFood() {
        int requiredFood = calculateRequiredUnitFood();

        ResourceStockpile stockpile =
                gameState.getTownHall().getStockpile();

        int availableFood =
                stockpile.getAmount(ResourceType.FOOD);

        int paidFood = Math.min(
                requiredFood,
                availableFood
        );

        if (paidFood > 0) {
            stockpile.spend(ResourceType.FOOD, paidFood);
        }

        boolean starving = paidFood < requiredFood;

        if (starving) {
            applyStarvationPenalty();
        }

        return new FoodPayment(
                requiredFood,
                paidFood,
                starving
        );
    }

    private BuildingUpkeepResult payBuildingUpkeep() {
        ResourceStockpile stockpile =
                gameState.getTownHall().getStockpile();

        int destroyedBuildings = 0;

        List<ResourceBuilding> buildings =
                new ArrayList<>(
                        gameState.getResourceBuildings()
                );

        for (ResourceBuilding building : buildings) {
            if (stockpile.canAfford(
                    building.getBuildingType()
                            .getUpkeepCost()
            )) {
                stockpile.spend(
                        building.getBuildingType()
                                .getUpkeepCost()
                );
                building.recordUpkeepPaid();
                continue;
            }

            building.recordMissedUpkeep();

            if (building.shouldBeDestroyedForUpkeep()) {
                gameState.removeResourceBuilding(building);
                destroyedBuildings = Math.incrementExact(
                        destroyedBuildings
                );
            }
        }

        int buildingsAtRisk = 0;

        for (ResourceBuilding building :
                gameState.getResourceBuildings()) {
            if (building.isAtRiskFromUpkeep()) {
                buildingsAtRisk = Math.incrementExact(
                        buildingsAtRisk
                );
            }
        }

        return new BuildingUpkeepResult(
                buildingsAtRisk,
                destroyedBuildings
        );
    }

    private void applyStarvationPenalty() {
        for (Unit unit : gameState.getUnits()) {
            unit.applyActionPointPenalty(
                    STARVATION_ACTION_POINT_PENALTY
            );
        }
    }

    private static final class FoodPayment {

        private final int requiredFood;
        private final int paidFood;
        private final boolean starving;

        private FoodPayment(
                int requiredFood,
                int paidFood,
                boolean starving
        ) {
            this.requiredFood = requiredFood;
            this.paidFood = paidFood;
            this.starving = starving;
        }
    }

    private static final class BuildingUpkeepResult {

        private final int buildingsAtRisk;
        private final int destroyedBuildings;

        private BuildingUpkeepResult(
                int buildingsAtRisk,
                int destroyedBuildings
        ) {
            this.buildingsAtRisk = buildingsAtRisk;
            this.destroyedBuildings = destroyedBuildings;
        }
    }
}
