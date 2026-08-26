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

        return gameState.canCreateUnit(unitType) &&
                isUnlocked(unitType) &&
                townHall.canStartUnitProduction(unitType);
    }

    public void start(UnitType unitType) {
        Objects.requireNonNull(
                unitType,
                "Unit type cannot be null"
        );

        if (!gameState.canCreateUnit(unitType)) {
            throw new IllegalStateException(
                    "Unit cap has been reached"
            );
        }

        if (!isUnlocked(unitType)) {
            throw new IllegalStateException(
                    unitType.getDisplayName() + " is not unlocked"
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

        if (!gameState.canCreateUnit(unitCommand.getUnitType())) {
            return null;
        }

        return gameState.deployUnit(
                unitCommand.getUnitType()
        );
    }

    private boolean isUnlocked(UnitType unitType) {
        int level = gameState.getTownHall()
                .getCurrentLevel().getLevelNumber();

        switch (unitType) {
            case ARCHER:
                return level >= 2;
            case CAVALRY:
                return level >= 2 &&
                        gameState.hasBuildingType(
                                ir.ac.ap.civilization.model.building.BuildingType.STABLE
                        );
            case CATAPULT:
                return level >= 3;
            case FISHING_BOAT:
            case GALLEY:
                return level >= 2 && gameState.getSpecialBuildings().stream()
                        .anyMatch(building -> building.getType() ==
                                ir.ac.ap.civilization.model.building.SpecialBuildingType.DOCK &&
                                building.getOwner().isPlayer() &&
                                !building.isDestroyed());
            case PIRATE:
                return false;
            default:
                return true;
        }
    }
}
