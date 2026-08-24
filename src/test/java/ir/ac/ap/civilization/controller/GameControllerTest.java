package ir.ac.ap.civilization.controller;

import ir.ac.ap.civilization.model.building.TownHall;
import ir.ac.ap.civilization.model.game.GameState;
import ir.ac.ap.civilization.model.resource.ResourceType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import ir.ac.ap.civilization.model.technology.TechnologyType;

class GameControllerTest {

    @Test
    void endTurnShouldBeDelegatedToTurnManager() {
        GameState gameState = new GameState();

        GameController controller =
                new GameController(gameState);

        controller.endTurn();

        assertEquals(2, gameState.getTurnNumber());

        assertEquals(
                64,
                gameState.getTownHall()
                        .getStockpile()
                        .getAmount(ResourceType.FOOD)
        );

        assertEquals(
                84,
                gameState.getTownHall()
                        .getStockpile()
                        .getAmount(ResourceType.WOOD)
        );
    }

    @Test
    void controllerShouldStartTownHallUpgrade() {
        GameState gameState = new GameState();

        GameController controller =
                new GameController(gameState);

        controller.startTownHallUpgrade();

        TownHall townHall =
                gameState.getTownHall();

        assertTrue(townHall.isBusy());

        assertEquals(
                3,
                townHall.getRemainingProductionTurns()
        );

        assertEquals(
                30,
                townHall.getStockpile()
                        .getAmount(ResourceType.WOOD)
        );

        assertEquals(
                10,
                townHall.getStockpile()
                        .getAmount(ResourceType.STONE)
        );
    }

    @Test
    void controllerShouldCancelTownHallCommand() {
        GameState gameState = new GameState();

        GameController controller =
                new GameController(gameState);

        controller.startTownHallUpgrade();
        controller.cancelTownHallCommand();

        TownHall townHall =
                gameState.getTownHall();

        assertFalse(townHall.isBusy());

        assertEquals(
                30,
                townHall.getStockpile()
                        .getAmount(ResourceType.WOOD)
        );

        assertEquals(
                10,
                townHall.getStockpile()
                        .getAmount(ResourceType.STONE)
        );
    }

    @Test
    void controllerShouldRejectNullGameState() {
        assertThrows(
                NullPointerException.class,
                () -> new GameController(null)
        );
    }
    @Test
    void controllerShouldStartTechnologyResearch() {
        GameState gameState = new GameState();

        GameController controller =
                new GameController(gameState);

        assertTrue(
                controller.canStartTechnologyResearch(
                        TechnologyType.STONE_MINING
                )
        );

        controller.startTechnologyResearch(
                TechnologyType.STONE_MINING
        );

        assertEquals(
                "Research Stone Mining",
                gameState.getTownHall()
                        .getActiveCommandName()
        );
    }
}