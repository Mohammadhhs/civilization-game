package ir.ac.ap.civilization.model.game;

import ir.ac.ap.civilization.model.building.BuildingType;
import ir.ac.ap.civilization.model.building.ResourceBuilding;
import ir.ac.ap.civilization.model.map.NaturalResourceType;
import ir.ac.ap.civilization.model.resource.ResourceType;
import ir.ac.ap.civilization.model.unit.Unit;
import ir.ac.ap.civilization.model.unit.Worker;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EconomyServiceTest {

    @Test
    void everyUnitShouldConsumeTwoFood() {
        GameState gameState = new GameState();
        EconomyService economyService =
                new EconomyService(gameState);

        economyService.applyEndTurnCosts();

        assertEquals(
                10,
                gameState.getEconomyStatus()
                        .getRequiredUnitFood()
        );
        assertEquals(
                10,
                gameState.getEconomyStatus()
                        .getPaidUnitFood()
        );
        assertEquals(
                50,
                gameState.getTownHall()
                        .getStockpile()
                        .getAmount(ResourceType.FOOD)
        );
        assertFalse(
                gameState.getEconomyStatus().isStarving()
        );
    }

    @Test
    void insufficientFoodShouldStartStarvationWithoutNegativeStock() {
        GameState gameState = new GameState();

        gameState.getTownHall().getStockpile().spend(
                ResourceType.FOOD,
                55
        );

        new EconomyService(gameState).applyEndTurnCosts();

        EconomyStatus status = gameState.getEconomyStatus();

        assertTrue(status.isStarving());
        assertFalse(status.canCityGrow());
        assertEquals(10, status.getRequiredUnitFood());
        assertEquals(5, status.getPaidUnitFood());
        assertEquals(5, status.getFoodDeficit());
        assertEquals(
                0,
                gameState.getTownHall()
                        .getStockpile()
                        .getAmount(ResourceType.FOOD)
        );

        for (Unit unit : gameState.getUnits()) {
            assertEquals(
                    unit.getMaximumActionPoints() - 2,
                    unit.getCurrentActionPoints()
            );
        }
    }

    @Test
    void adequateFoodShouldEndStarvationNextTurn() {
        GameState gameState = new GameState();
        EconomyService economyService =
                new EconomyService(gameState);

        gameState.getTownHall().getStockpile().spend(
                ResourceType.FOOD,
                55
        );

        economyService.applyEndTurnCosts();
        assertTrue(gameState.getEconomyStatus().isStarving());

        gameState.getTownHall().getStockpile().add(
                ResourceType.FOOD,
                10
        );
        gameState.restoreAllUnitActionPoints();
        economyService.applyEndTurnCosts();

        assertFalse(gameState.getEconomyStatus().isStarving());
        assertTrue(gameState.getEconomyStatus().canCityGrow());

        for (Unit unit : gameState.getUnits()) {
            assertEquals(
                    unit.getMaximumActionPoints(),
                    unit.getCurrentActionPoints()
            );
        }
    }

    @Test
    void productionShouldBeAppliedBeforeFoodUpkeep() {
        GameState gameState = new GameState();

        gameState.getTownHall().getStockpile().spend(
                ResourceType.FOOD,
                54
        );

        new TurnManager(gameState).endTurn();

        assertEquals(
                0,
                gameState.getTownHall()
                        .getStockpile()
                        .getAmount(ResourceType.FOOD)
        );
        assertFalse(
                gameState.getEconomyStatus().isStarving()
        );
    }

    @Test
    void endTurnStarvationShouldReduceRestoredActionPoints() {
        GameState gameState = new GameState();

        gameState.getTownHall().getStockpile().spend(
                ResourceType.FOOD,
                55
        );

        new TurnManager(gameState).endTurn();

        assertTrue(gameState.getEconomyStatus().isStarving());
        assertEquals(1, gameState.getEconomyStatus().getFoodDeficit());

        for (Unit unit : gameState.getUnits()) {
            assertEquals(
                    unit.getMaximumActionPoints() - 2,
                    unit.getCurrentActionPoints()
            );
        }
    }

    @Test
    void buildingShouldPayUpkeepEveryTurn() {
        GameState gameState = new GameState();
        ResourceBuilding building =
                addLumberMill(gameState);

        new EconomyService(gameState).applyEndTurnCosts();

        assertEquals(
                79,
                gameState.getTownHall()
                        .getStockpile()
                        .getAmount(ResourceType.WOOD)
        );
        assertEquals(
                0,
                building.getConsecutiveMissedUpkeepTurns()
        );
        assertFalse(building.isAtRiskFromUpkeep());
    }

    @Test
    void thirdConsecutiveMissedUpkeepShouldDestroyBuilding() {
        GameState gameState = new GameState();
        ResourceBuilding building =
                addLumberMill(gameState);

        Worker worker = (Worker) gameState
                .getUnitById(3)
                .orElseThrow(AssertionError::new);

        building.stationWorker(worker.getId());
        worker.stationAt(building.getId());

        gameState.getTownHall().getStockpile().spend(
                ResourceType.WOOD,
                80
        );

        EconomyService economyService =
                new EconomyService(gameState);

        economyService.applyEndTurnCosts();
        assertEquals(1, building.getConsecutiveMissedUpkeepTurns());
        assertTrue(gameState.getResourceBuildingById(
                building.getId()
        ).isPresent());

        economyService.applyEndTurnCosts();
        assertEquals(2, building.getConsecutiveMissedUpkeepTurns());
        assertTrue(gameState.getResourceBuildingById(
                building.getId()
        ).isPresent());

        economyService.applyEndTurnCosts();

        assertFalse(gameState.getResourceBuildingById(
                building.getId()
        ).isPresent());
        assertFalse(worker.isStationed());
        assertEquals(
                1,
                gameState.getEconomyStatus()
                        .getBuildingsDestroyedLastTurn()
        );
    }

    @Test
    void successfulUpkeepShouldResetConsecutiveMisses() {
        GameState gameState = new GameState();
        ResourceBuilding building =
                addLumberMill(gameState);

        gameState.getTownHall().getStockpile().spend(
                ResourceType.WOOD,
                80
        );

        EconomyService economyService =
                new EconomyService(gameState);

        economyService.applyEndTurnCosts();
        assertEquals(1, building.getConsecutiveMissedUpkeepTurns());

        gameState.getTownHall().getStockpile().add(
                ResourceType.WOOD,
                1
        );
        economyService.applyEndTurnCosts();

        assertEquals(0, building.getConsecutiveMissedUpkeepTurns());
        assertEquals(
                0,
                gameState.getEconomyStatus()
                        .getBuildingsAtRisk()
        );

        economyService.applyEndTurnCosts();

        assertEquals(1, building.getConsecutiveMissedUpkeepTurns());
        assertTrue(gameState.getResourceBuildingById(
                building.getId()
        ).isPresent());
    }

    @Test
    void economyServiceShouldRejectNullGameState() {
        assertThrows(
                NullPointerException.class,
                () -> new EconomyService(null)
        );
    }

    private ResourceBuilding addLumberMill(
            GameState gameState
    ) {
        Worker worker = (Worker) gameState
                .getUnitById(3)
                .orElseThrow(AssertionError::new);

        return gameState.addResourceBuilding(
                BuildingType.LUMBER_MILL,
                worker.getPosition(),
                NaturalResourceType.WOOD
        );
    }
}
