package ir.ac.ap.civilization.model.game;

import ir.ac.ap.civilization.model.building.TownHall;
import ir.ac.ap.civilization.model.production.ProductionCommand;
import ir.ac.ap.civilization.model.production.UnitProductionCommand;
import ir.ac.ap.civilization.model.unit.Unit;
import ir.ac.ap.civilization.model.unit.UnitType;

import java.util.Objects;

public final class UnitProductionService {

    private final GameState gameState;

    public UnitProductionService(GameState gameState) {
        this.gameState = Objects.requireNonNull(
                gameState,
                "Game state cannot be null"
        );
    }

    public boolean canStart(UnitType unitType) {
        Objects.requireNonNull(
                unitType,
                "Unit type cannot be null"
        );

        TownHall townHall = gameState.getTownHall();

        return gameState.canCreateUnit() &&
                townHall.canStartUnitProduction(unitType);
    }

    public void start(UnitType unitType) {
        Objects.requireNonNull(
                unitType,
                "Unit type cannot be null"
        );

        if (!gameState.canCreateUnit()) {
            throw new IllegalStateException(
                    "Unit cap has been reached"
            );
        }

        gameState.getTownHall().startUnitProduction(unitType);
    }

    public Unit deployCompletedCommand(
            ProductionCommand completedCommand
    ) {
        if (!(completedCommand instanceof
                UnitProductionCommand)) {
            return null;
        }

        UnitProductionCommand unitCommand =
                (UnitProductionCommand) completedCommand;

        if (!gameState.canCreateUnit()) {
            return null;
        }

        return gameState.deployUnit(
                unitCommand.getUnitType()
        );
    }
}
