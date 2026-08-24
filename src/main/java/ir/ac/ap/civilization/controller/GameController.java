package ir.ac.ap.civilization.controller;

import ir.ac.ap.civilization.model.game.GameState;
import ir.ac.ap.civilization.model.game.TurnManager;
import ir.ac.ap.civilization.model.map.HexCoordinate;
import ir.ac.ap.civilization.model.map.HexTile;
import ir.ac.ap.civilization.model.technology.TechnologyType;
import ir.ac.ap.civilization.model.unit.UnitMovementResult;
import ir.ac.ap.civilization.model.unit.UnitMovementService;

import java.util.List;
import java.util.Objects;

public final class GameController {

    private final GameState gameState;
    private final TurnManager turnManager;
    private final UnitMovementService unitMovementService;

    public GameController(GameState gameState) {
        this.gameState = Objects.requireNonNull(
                gameState,
                "Game state cannot be null"
        );

        this.turnManager = new TurnManager(gameState);

        this.unitMovementService =
                new UnitMovementService(gameState);
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

    public void startTechnologyResearch(
            TechnologyType technologyType
    ) {
        gameState.getTownHall()
                .startResearch(technologyType);
    }

    public boolean canStartTechnologyResearch(
            TechnologyType technologyType
    ) {
        return gameState.getTownHall()
                .canStartResearch(technologyType);
    }

    public boolean canMoveUnit(
            int unitId,
            HexCoordinate destination
    ) {
        return unitMovementService.canMoveUnit(
                unitId,
                destination
        );
    }

    public List<HexTile> getValidMovementDestinations(
            int unitId
    ) {
        return unitMovementService
                .getValidMovementDestinations(unitId);
    }

    public UnitMovementResult moveUnit(
            int unitId,
            HexCoordinate destination
    ) {
        return unitMovementService.moveUnit(
                unitId,
                destination
        );
    }
}