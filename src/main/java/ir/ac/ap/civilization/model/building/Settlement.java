package ir.ac.ap.civilization.model.building;

import ir.ac.ap.civilization.model.entity.EntityKey;
import ir.ac.ap.civilization.model.entity.EntityKind;
import ir.ac.ap.civilization.model.entity.HitPoints;
import ir.ac.ap.civilization.model.entity.Owner;
import ir.ac.ap.civilization.model.map.HexCoordinate;
import ir.ac.ap.civilization.model.resource.ResourceType;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

public final class Settlement implements MaintainedBuilding {

    public static final int UNIT_CAP_BONUS = 5;
    public static final int CONSTRUCTION_ACTION_POINT_COST = 3;
    private static final int VISION_RADIUS = 2;
    private static final int MAXIMUM_HIT_POINTS = 150;

    private static final Map<ResourceType, Integer>
            CONSTRUCTION_COST = createCost(60, 50, 20);

    private static final Map<ResourceType, Integer>
            UPKEEP_COST = createCost(1, 1, 0);

    private final int id;
    private final HexCoordinate position;
    private final Owner owner;
    private final HitPoints hitPoints;

    private int consecutiveMissedUpkeepTurns;

    public Settlement(
            int id,
            HexCoordinate position
    ) {
        this(id, position, Owner.PLAYER,
                MAXIMUM_HIT_POINTS, MAXIMUM_HIT_POINTS);
    }

    public Settlement(
            int id,
            HexCoordinate position,
            Owner owner,
            int currentHitPoints,
            int maximumHitPoints
    ) {
        if (id <= 0) {
            throw new IllegalArgumentException(
                    "Settlement id must be positive"
            );
        }

        this.id = id;
        this.position = Objects.requireNonNull(
                position,
                "Settlement position cannot be null"
        );
        this.owner = Objects.requireNonNull(
                owner,
                "Settlement owner cannot be null"
        );
        this.hitPoints = new HitPoints(
                currentHitPoints,
                maximumHitPoints
        );
        this.consecutiveMissedUpkeepTurns = 0;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public EntityKey getEntityKey() {
        return new EntityKey(EntityKind.BUILDING, id);
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

    @Override
    public HexCoordinate getPosition() {
        return position;
    }

    @Override
    public int getVisionRadius() {
        return VISION_RADIUS;
    }

    public int getUnitCapBonus() {
        return UNIT_CAP_BONUS;
    }

    public static Map<ResourceType, Integer>
    getConstructionCost() {
        return CONSTRUCTION_COST;
    }

    @Override
    public Map<ResourceType, Integer> getUpkeepCost() {
        return UPKEEP_COST;
    }

    @Override
    public int getConsecutiveMissedUpkeepTurns() {
        return consecutiveMissedUpkeepTurns;
    }

    @Override
    public boolean isAtRiskFromUpkeep() {
        return consecutiveMissedUpkeepTurns > 0;
    }

    @Override
    public boolean shouldBeDestroyedForUpkeep() {
        return consecutiveMissedUpkeepTurns >=
                MAXIMUM_CONSECUTIVE_MISSED_UPKEEP_TURNS;
    }

    @Override
    public void recordUpkeepPaid() {
        consecutiveMissedUpkeepTurns = 0;
    }

    @Override
    public void recordMissedUpkeep() {
        consecutiveMissedUpkeepTurns = Math.incrementExact(
                consecutiveMissedUpkeepTurns
        );
    }

    private static Map<ResourceType, Integer> createCost(
            int wood,
            int stone,
            int iron
    ) {
        EnumMap<ResourceType, Integer> cost =
                new EnumMap<>(ResourceType.class);

        if (wood > 0) {
            cost.put(ResourceType.WOOD, wood);
        }

        if (stone > 0) {
            cost.put(ResourceType.STONE, stone);
        }

        if (iron > 0) {
            cost.put(ResourceType.IRON, iron);
        }

        return Collections.unmodifiableMap(cost);
    }
}
