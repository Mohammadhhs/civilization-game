package ir.ac.ap.civilization.model.unit;

import ir.ac.ap.civilization.model.map.HexCoordinate;

public final class Builder extends Unit {

    private static final int MAXIMUM_ACTION_POINTS = 5;
    private static final int VISION_RADIUS = 1;
    private static final int INITIAL_CHARGES = 3;

    private int remainingCharges;

    public Builder(int id, HexCoordinate position) {
        super(
                id,
                UnitType.BUILDER,
                MAXIMUM_ACTION_POINTS,
                VISION_RADIUS,
                position
        );

        this.remainingCharges = INITIAL_CHARGES;
    }

    public int getRemainingCharges() {
        return remainingCharges;
    }

    public boolean hasCharges() {
        return remainingCharges > 0;
    }

    public void useCharge() {
        if (!hasCharges()) {
            throw new IllegalStateException(
                    "Builder has no remaining charges"
            );
        }

        remainingCharges--;
    }
}
