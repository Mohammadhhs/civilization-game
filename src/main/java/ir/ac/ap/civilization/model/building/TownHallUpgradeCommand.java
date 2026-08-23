package ir.ac.ap.civilization.model.building;

import ir.ac.ap.civilization.model.production.ProductionCommand;

import java.util.Objects;

public final class TownHallUpgradeCommand
        implements ProductionCommand {

    private final TownHallLevel targetLevel;
    private int remainingTurns;

    public TownHallUpgradeCommand(TownHallLevel targetLevel) {
        this.targetLevel = Objects.requireNonNull(
                targetLevel,
                "Target level cannot be null"
        );

        if (targetLevel == TownHallLevel.BASE_CAMP) {
            throw new IllegalArgumentException(
                    "Cannot upgrade Town Hall to Base Camp"
            );
        }

        this.remainingTurns = targetLevel.getUpgradeTurns();
    }

    public TownHallLevel getTargetLevel() {
        return targetLevel;
    }

    @Override
    public String getName() {
        return "Upgrade Town Hall to " +
                targetLevel.getDisplayName();
    }

    @Override
    public int getRemainingTurns() {
        return remainingTurns;
    }

    @Override
    public void advanceTurn() {
        if (remainingTurns > 0) {
            remainingTurns--;
        }
    }

    @Override
    public boolean isCompleted() {
        return remainingTurns == 0;
    }

    @Override
    public void complete(TownHall townHall) {
        Objects.requireNonNull(
                townHall,
                "Town Hall cannot be null"
        );

        if (!isCompleted()) {
            throw new IllegalStateException(
                    "Upgrade command is not completed yet"
            );
        }

        townHall.applyUpgrade(targetLevel);
    }
}