package ir.ac.ap.civilization.model.unit;

import ir.ac.ap.civilization.model.entity.Damageable;
import ir.ac.ap.civilization.model.entity.EntityKey;
import ir.ac.ap.civilization.model.entity.EntityKind;
import ir.ac.ap.civilization.model.entity.GameEntity;
import ir.ac.ap.civilization.model.entity.HitPoints;
import ir.ac.ap.civilization.model.entity.Owner;
import ir.ac.ap.civilization.model.entity.OwnedEntity;
import ir.ac.ap.civilization.model.map.HexCoordinate;

import java.util.Objects;

public abstract class Unit implements
        GameEntity, OwnedEntity, Damageable {

    private static final int DEFAULT_MAXIMUM_HIT_POINTS = 100;

    private final int id;
    private final UnitType unitType;
    private final int maximumActionPoints;
    private final int visionRadius;
    private final Owner owner;
    private final HitPoints hitPoints;

    private int currentActionPoints;
    private HexCoordinate position;

    protected Unit(
            int id,
            UnitType unitType,
            int maximumActionPoints,
            int visionRadius,
            HexCoordinate position
    ) {
        this(id, unitType, maximumActionPoints, visionRadius,
                position, Owner.PLAYER,
                DEFAULT_MAXIMUM_HIT_POINTS,
                DEFAULT_MAXIMUM_HIT_POINTS);
    }

    protected Unit(
            int id,
            UnitType unitType,
            int maximumActionPoints,
            int visionRadius,
            HexCoordinate position,
            Owner owner,
            int currentHitPoints,
            int maximumHitPoints
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
        this.owner = Objects.requireNonNull(
                owner,
                "Unit owner cannot be null"
        );
        this.hitPoints = new HitPoints(
                currentHitPoints,
                maximumHitPoints
        );

        this.position = Objects.requireNonNull(
                position,
                "Unit position cannot be null"
        );
    }

    public int getId() {
        return id;
    }

    @Override
    public EntityKey getEntityKey() {
        return new EntityKey(EntityKind.UNIT, id);
    }

    @Override
    public Owner getOwner() {
        return owner;
    }

    @Override
    public int getCurrentHitPoints() {
        return hitPoints.getCurrent();
    }

    @Override
    public int getMaximumHitPoints() {
        return hitPoints.getMaximum();
    }

    @Override
    public void takeDamage(int amount) {
        hitPoints.damage(amount);
    }

    @Override
    public void heal(int amount) {
        hitPoints.heal(amount);
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
