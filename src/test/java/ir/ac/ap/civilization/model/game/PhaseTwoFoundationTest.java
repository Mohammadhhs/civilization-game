package ir.ac.ap.civilization.model.game;

import ir.ac.ap.civilization.model.entity.EntityKey;
import ir.ac.ap.civilization.model.entity.EntityKind;
import ir.ac.ap.civilization.model.entity.Owner;
import ir.ac.ap.civilization.model.unit.Unit;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PhaseTwoFoundationTest {

    @Test
    void existingEntitiesShouldExposeCompatiblePhaseTwoContracts() {
        GameState gameState = new GameState(123L);
        Unit firstUnit = gameState.getUnits().get(0);

        assertEquals(Owner.PLAYER, firstUnit.getOwner());
        assertEquals(100, firstUnit.getMaximumHitPoints());
        assertEquals(
                new EntityKey(EntityKind.UNIT, firstUnit.getId()),
                firstUnit.getEntityKey()
        );
        assertSame(
                firstUnit,
                gameState.getEntity(firstUnit.getEntityKey()).get()
        );
        assertSame(
                gameState.getTownHall(),
                gameState.getEntity(
                        new EntityKey(EntityKind.TOWN_HALL, 1)
                ).get()
        );
        assertFalse(gameState.getEntity(
                new EntityKey(EntityKind.TRIBE, 1)
        ).isPresent());
    }

    @Test
    void endTurnShouldExposeOrderedCompletedPhases() {
        GameState gameState = new GameState(456L);
        TurnManager turnManager = new TurnManager(gameState);

        assertTrue(gameState.isStableForPersistence());
        turnManager.endTurn();

        assertTrue(gameState.isStableForPersistence());
        assertEquals(GameOperation.STABLE,
                gameState.getCurrentOperation());
        assertEquals(
                Arrays.asList(TurnPhase.values()),
                turnManager.getLastCompletedPhases()
        );
    }
}
