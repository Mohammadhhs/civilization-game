package ir.ac.ap.civilization.model.unit;

import ir.ac.ap.civilization.model.map.HexCoordinate;

import java.util.Objects;

public abstract class Unit {

    private final int id;
    private final UnitType unitType;
    private final int maximumActionPoints;
    private final int visionRadius;

    private int currentActionPoints;
    private HexCoordinate position;

    protected Unit(
            int id,
            UnitType unitType,
            int maximumActionPoints,
            int visionRadius,
            HexCoordinate position
    ) {
        if (id <= 0) {
            throw new IllegalArgumentException(
                    "Unit id must be positive"
            );
        }

        if (maximumActionPoints <= 0) {
            throw new IllegalArgumentException(
                    "Maximum action points must be positive"
            );
        }

        if (visionRadius < 0) {
            throw new IllegalArgumentException(
                    "Vision radius cannot be negative"
            );
        }

        this.id = id;

        this.unitType = Objects.requireNonNull(
                unitType,
                "Unit type cannot be null"
        );

        this.maximumActionPoints = maximumActionPoints;
        this.currentActionPoints = maximumActionPoints;
        this.visionRadius = visionRadius;

        this.position = Objects.requireNonNull(
                position,
                "Unit position cannot be null"
        );
    }

    public int getId() {
        return id;
    }

    public UnitType getUnitType() {
        return unitType;
    }

    public String getDisplayName() {
        return unitType.getDisplayName();
    }

    public String getMapSymbol() {
        return unitType.getMapSymbol();
    }

    public int getMaximumActionPoints() {
        return maximumActionPoints;
    }

    public int getCurrentActionPoints() {
        return currentActionPoints;
    }

    public int getVisionRadius() {
        return visionRadius;
    }

    public HexCoordinate getPosition() {
        return position;
    }

    public boolean hasActionPoints() {
        return currentActionPoints > 0;
    }

    public boolean canSpendActionPoints(int cost) {
        validateActionPointCost(cost);
        return currentActionPoints >= cost;
    }

    public void spendActionPoints(int cost) {
        validateActionPointCost(cost);

        if (!canSpendActionPoints(cost)) {
            throw new IllegalStateException(
                    "Unit does not have enough action points. " +
                            "Required: " + cost +
                            ", available: " +
                            currentActionPoints
            );
        }

        currentActionPoints -= cost;
    }

    public void restoreActionPoints() {
        currentActionPoints = maximumActionPoints;
    }

    public void applyActionPointPenalty(int penalty) {
        validateActionPointCost(penalty);

        currentActionPoints = Math.max(
                0,
                currentActionPoints - penalty
        );
    }

    void moveTo(HexCoordinate destination) {
        this.position = Objects.requireNonNull(
                destination,
                "Movement destination cannot be null"
        );
    }

    private void validateActionPointCost(int cost) {
        if (cost < 0) {
            throw new IllegalArgumentException(
                    "Action point cost cannot be negative"
            );
        }
    }

    @Override
    public String toString() {
        return "Unit{" +
                "id=" + id +
                ", unitType=" + unitType +
                ", currentActionPoints=" +
                currentActionPoints +
                ", maximumActionPoints=" +
                maximumActionPoints +
                ", visionRadius=" + visionRadius +
                ", position=" + position +
                '}';
    }
}
