package ir.ac.ap.civilization.controller;

import ir.ac.ap.civilization.model.game.GameState;
import ir.ac.ap.civilization.model.game.TurnManager;

import java.util.Objects;

public final class GameController {

    private final GameState gameState;
    private final TurnManager turnManager;

    public GameController(GameState gameState) {
        this.gameState = Objects.requireNonNull(
                gameState,
                "Game state cannot be null"
        );

        this.turnManager =
                new TurnManager(gameState);
    }

    public GameState getGameState() {
        return gameState;
    }

    public void endTurn() {
        turnManager.endTurn();
    }

    public void startTownHallUpgrade() {
        gameState.getTownHall().startUpgrade();
    }

    public void cancelTownHallCommand() {
        gameState.getTownHall()
                .cancelCurrentCommand();
    }
}