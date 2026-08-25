package ir.ac.ap.civilization.model.unit;

import ir.ac.ap.civilization.model.map.HexCoordinate;

import java.util.Objects;

public final class UnitMovementResult {

    private final int unitId;
    private final HexCoordinate start;
    private final HexCoordinate destination;
    private final int actionPointCost;
    private final int remainingActionPoints;

    public UnitMovementResult(
            int unitId,
            HexCoordinate start,
            HexCoordinate destination,
            int actionPointCost,
            int remainingActionPoints
    ) {
        if (unitId <= 0) {
            throw new IllegalArgumentException(
                    "Unit id must be positive"
            );
        }

        if (actionPointCost <= 0) {
            throw new IllegalArgumentException(
                    "Movement cost must be positive"
            );
        }

        if (remainingActionPoints < 0) {
            throw new IllegalArgumentException(
                    "Remaining action points cannot be negative"
            );
        }

        this.unitId = unitId;

        this.start = Objects.requireNonNull(
                start,
                "Movement start cannot be null"
        );

        this.destination = Objects.requireNonNull(
                destination,
                "Movement destination cannot be null"
        );

        this.actionPointCost = actionPointCost;
        this.remainingActionPoints = remainingActionPoints;
    }

    public int getUnitId() {
        return unitId;
    }

    public HexCoordinate getStart() {
        return start;
    }

    public HexCoordinate getDestination() {
        return destination;
    }

    public int getActionPointCost() {
        return actionPointCost;
    }

    public int getRemainingActionPoints() {
        return remainingActionPoints;
    }
}
