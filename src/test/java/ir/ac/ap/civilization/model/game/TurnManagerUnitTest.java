package ir.ac.ap.civilization.model.game;

import ir.ac.ap.civilization.model.unit.Unit;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TurnManagerUnitTest {

    @Test
    void endTurnRestoresActionPointsForEveryUnit() {
        GameState gameState = new GameState();
        TurnManager turnManager =
                new TurnManager(gameState);

        for (Unit unit : gameState.getUnits()) {
            unit.spendActionPoints(1);

            assertEquals(
                    unit.getMaximumActionPoints() - 1,
                    unit.getCurrentActionPoints()
            );
        }

        turnManager.endTurn();

        assertEquals(2, gameState.getTurnNumber());

        for (Unit unit : gameState.getUnits()) {
            assertEquals(
                    unit.getMaximumActionPoints(),
                    unit.getCurrentActionPoints()
            );
        }
    }
}
