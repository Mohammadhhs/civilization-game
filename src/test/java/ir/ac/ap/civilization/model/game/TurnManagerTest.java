package ir.ac.ap.civilization.model.game;

import ir.ac.ap.civilization.model.building.TownHall;
import ir.ac.ap.civilization.model.building.TownHallLevel;
import ir.ac.ap.civilization.model.resource.ResourceStockpile;
import ir.ac.ap.civilization.model.resource.ResourceType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TurnManagerTest {

    @Test
    void newGameShouldHaveCorrectInitialState() {
        GameState gameState = new GameState();

        assertEquals(1, gameState.getTurnNumber());

        TownHall townHall =
                gameState.getTownHall();

        assertEquals(
                TownHallLevel.BASE_CAMP,
                townHall.getCurrentLevel()
        );

        ResourceStockpile stockpile =
                townHall.getStockpile();

        assertEquals(
                60,
                stockpile.getAmount(ResourceType.FOOD)
        );

        assertEquals(
                80,
                stockpile.getAmount(ResourceType.WOOD)
        );

        assertEquals(
                60,
                stockpile.getAmount(ResourceType.STONE)
        );

        assertEquals(
                0,
                stockpile.getAmount(ResourceType.IRON)
        );
    }

    @Test
    void endTurnShouldProduceResourcesAndIncreaseTurnNumber() {
        GameState gameState = new GameState();
        TurnManager turnManager =
                new TurnManager(gameState);

        turnManager.endTurn();

        assertEquals(2, gameState.getTurnNumber());

        ResourceStockpile stockpile =
                gameState.getTownHall().getStockpile();

        assertEquals(
                64,
                stockpile.getAmount(ResourceType.FOOD)
        );

        assertEquals(
                84,
                stockpile.getAmount(ResourceType.WOOD)
        );

        assertEquals(
                60,
                stockpile.getAmount(ResourceType.STONE)
        );

        assertEquals(
                0,
                stockpile.getAmount(ResourceType.IRON)
        );
    }

    @Test
    void endTurnShouldApplyStorageCapacityLimit() {
        GameState gameState = new GameState();
        TurnManager turnManager =
                new TurnManager(gameState);

        ResourceStockpile stockpile =
                gameState.getTownHall().getStockpile();

        stockpile.add(ResourceType.FOOD, 60);

        assertEquals(
                120,
                stockpile.getAmount(ResourceType.FOOD)
        );

        turnManager.endTurn();

        assertEquals(
                100,
                stockpile.getAmount(ResourceType.FOOD)
        );
    }

    @Test
    void endTurnShouldAdvanceTownHallUpgradeQueue() {
        GameState gameState = new GameState();
        TurnManager turnManager =
                new TurnManager(gameState);

        TownHall townHall =
                gameState.getTownHall();

        townHall.startUpgrade();

        assertEquals(
                3,
                townHall.getRemainingProductionTurns()
        );

        turnManager.endTurn();

        assertEquals(2, gameState.getTurnNumber());
        assertEquals(
                2,
                townHall.getRemainingProductionTurns()
        );

        turnManager.endTurn();

        assertEquals(3, gameState.getTurnNumber());
        assertEquals(
                1,
                townHall.getRemainingProductionTurns()
        );

        turnManager.endTurn();

        assertEquals(4, gameState.getTurnNumber());

        assertEquals(
                TownHallLevel.SETTLEMENT,
                townHall.getCurrentLevel()
        );

        assertEquals(
                200,
                townHall.getStockpile()
                        .getCapacityPerResource()
        );

        assertFalse(townHall.isBusy());
    }

    @Test
    void completedUpgradeShouldApplyNewCapacityBeforeOverflowRemoval() {
        GameState gameState = new GameState();
        TurnManager turnManager =
                new TurnManager(gameState);

        TownHall townHall =
                gameState.getTownHall();

        ResourceStockpile stockpile =
                townHall.getStockpile();

        townHall.startUpgrade();

        turnManager.endTurn();
        turnManager.endTurn();

        assertEquals(
                1,
                townHall.getRemainingProductionTurns()
        );

        stockpile.add(ResourceType.FOOD, 100);

        assertEquals(
                168,
                stockpile.getAmount(ResourceType.FOOD)
        );

        turnManager.endTurn();

        assertEquals(
                TownHallLevel.SETTLEMENT,
                townHall.getCurrentLevel()
        );

        assertEquals(
                200,
                stockpile.getCapacityPerResource()
        );

        assertEquals(
                172,
                stockpile.getAmount(ResourceType.FOOD)
        );
    }

    @Test
    void threeTurnsShouldProduceResourcesWhileUpgradeIsActive() {
        GameState gameState = new GameState();
        TurnManager turnManager =
                new TurnManager(gameState);

        TownHall townHall =
                gameState.getTownHall();

        townHall.startUpgrade();

        turnManager.endTurn();
        turnManager.endTurn();
        turnManager.endTurn();

        ResourceStockpile stockpile =
                townHall.getStockpile();

        assertEquals(
                72,
                stockpile.getAmount(ResourceType.FOOD)
        );

        assertEquals(
                42,
                stockpile.getAmount(ResourceType.WOOD)
        );

        assertEquals(
                10,
                stockpile.getAmount(ResourceType.STONE)
        );
    }

    @Test
    void turnManagerShouldRejectNullGameState() {
        assertThrows(
                NullPointerException.class,
                () -> new TurnManager(null)
        );
    }
}