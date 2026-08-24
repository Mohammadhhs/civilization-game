package ir.ac.ap.civilization.model.game;

import ir.ac.ap.civilization.model.building.TownHall;
import ir.ac.ap.civilization.model.resource.ResourceStockpile;

import java.util.Objects;

public final class TurnManager {

    private final GameState gameState;

    public TurnManager(GameState gameState) {
        this.gameState = Objects.requireNonNull(
                gameState,
                "Game state cannot be null"
        );
    }

    public void endTurn() {
        TownHall townHall =
                gameState.getTownHall();

        ResourceStockpile stockpile =
                townHall.getStockpile();

        gameState.restoreAllUnitActionPoints();

        townHall.produceBaseResources();

        townHall.advanceProductionQueue();

        stockpile.applyCapacityLimit();

        gameState.incrementTurnNumber();
    }
}