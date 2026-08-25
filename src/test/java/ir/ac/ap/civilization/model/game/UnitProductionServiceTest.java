package ir.ac.ap.civilization.model.game;

import ir.ac.ap.civilization.model.resource.ResourceStockpile;
import ir.ac.ap.civilization.model.resource.ResourceType;
import ir.ac.ap.civilization.model.unit.UnitType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UnitProductionServiceTest {

    @Test
    void workerShouldBeProducedThroughTownHallQueue() {
        GameState gameState = new GameState();
        UnitProductionService service =
                new UnitProductionService(gameState);

        service.start(UnitType.WORKER);

        assertTrue(gameState.getTownHall().isBusy());
        assertEquals("Train Worker",
                gameState.getTownHall().getActiveCommandName());
        assertEquals(1,
                gameState.getTownHall().getRemainingProductionTurns());

        new TurnManager(gameState).endTurn();

        assertEquals(6, gameState.getActiveUnitCount());
        assertEquals(3, gameState.getActiveUnitCount(UnitType.WORKER));
        assertFalse(gameState.getTownHall().isBusy());
    }

    @Test
    void multiTurnUnitShouldAppearOnlyAfterCompletion() {
        GameState gameState = new GameState();
        UnitProductionService service =
                new UnitProductionService(gameState);

        service.start(UnitType.EXPLORER);

        new TurnManager(gameState).endTurn();
        assertEquals(5, gameState.getActiveUnitCount());
        assertEquals(1,
                gameState.getTownHall().getRemainingProductionTurns());

        new TurnManager(gameState).endTurn();
        assertEquals(6, gameState.getActiveUnitCount());
        assertEquals(2, gameState.getActiveUnitCount(UnitType.EXPLORER));
    }

    @Test
    void productionCostShouldBePaidAtomicallyAtStart() {
        GameState gameState = new GameState();
        ResourceStockpile stockpile =
                gameState.getTownHall().getStockpile();

        new UnitProductionService(gameState)
                .start(UnitType.BUILDER);

        assertEquals(50, stockpile.getAmount(ResourceType.WOOD));
        assertEquals(50, stockpile.getAmount(ResourceType.STONE));
        assertEquals(60, stockpile.getAmount(ResourceType.FOOD));
    }

    @Test
    void insufficientResourcesShouldNotStartOrSpend() {
        GameState gameState = new GameState();
        ResourceStockpile stockpile =
                gameState.getTownHall().getStockpile();

        stockpile.spend(ResourceType.WOOD, 80);

        UnitProductionService service =
                new UnitProductionService(gameState);

        assertFalse(service.canStart(UnitType.BUILDER));
        assertThrows(
                IllegalStateException.class,
                () -> service.start(UnitType.BUILDER)
        );
        assertEquals(0, stockpile.getAmount(ResourceType.WOOD));
        assertEquals(60, stockpile.getAmount(ResourceType.STONE));
        assertFalse(gameState.getTownHall().isBusy());
    }

    @Test
    void unitCapShouldBlockNewProduction() {
        GameState gameState = new GameState();

        gameState.deployUnit(UnitType.WORKER);
        gameState.deployUnit(UnitType.WORKER);
        gameState.deployUnit(UnitType.WORKER);

        UnitProductionService service =
                new UnitProductionService(gameState);

        assertEquals(8, gameState.getActiveUnitCount());
        assertFalse(service.canStart(UnitType.WORKER));
        assertThrows(
                IllegalStateException.class,
                () -> service.start(UnitType.WORKER)
        );
        assertFalse(gameState.getTownHall().isBusy());
    }

    @Test
    void busyTownHallShouldRejectSecondUnitCommand() {
        GameState gameState = new GameState();
        UnitProductionService service =
                new UnitProductionService(gameState);

        service.start(UnitType.WORKER);

        assertFalse(service.canStart(UnitType.EXPLORER));
        assertThrows(
                IllegalStateException.class,
                () -> service.start(UnitType.EXPLORER)
        );
    }

    @Test
    void completionShouldRecheckUnitCap() {
        GameState gameState = new GameState();
        UnitProductionService service =
                new UnitProductionService(gameState);

        service.start(UnitType.WORKER);

        gameState.deployUnit(UnitType.EXPLORER);
        gameState.deployUnit(UnitType.EXPLORER);
        gameState.deployUnit(UnitType.EXPLORER);

        new TurnManager(gameState).endTurn();

        assertEquals(8, gameState.getActiveUnitCount());
        assertFalse(gameState.isOverUnitCap());
        assertFalse(gameState.getTownHall().isBusy());
    }
}
