package ir.ac.ap.civilization.model.production;

import ir.ac.ap.civilization.model.building.TownHall;
import ir.ac.ap.civilization.model.unit.UnitType;

import java.util.Objects;

public final class UnitProductionCommand
        implements ProductionCommand {

    private final UnitType unitType;
    private int remainingTurns;

    public UnitProductionCommand(UnitType unitType) {
        this.unitType = Objects.requireNonNull(
                unitType,
                "Unit type cannot be null"
        );
        this.remainingTurns = unitType.getProductionTurns();
    }

    public UnitType getUnitType() {
        return unitType;
    }

    @Override
    public String getName() {
        return "Train " + unitType.getDisplayName();
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
    }
}
