package ir.ac.ap.civilization.model.building;

import ir.ac.ap.civilization.model.map.HexCoordinate;
import ir.ac.ap.civilization.model.resource.ResourceType;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

public final class Settlement implements MaintainedBuilding {

    public static final int UNIT_CAP_BONUS = 5;
    public static final int CONSTRUCTION_ACTION_POINT_COST = 3;

    private static final Map<ResourceType, Integer>
            CONSTRUCTION_COST = createCost(60, 50, 20);

    private static final Map<ResourceType, Integer>
            UPKEEP_COST = createCost(1, 1, 0);

    private final int id;
    private final HexCoordinate position;

    private int consecutiveMissedUpkeepTurns;

    public Settlement(
            int id,
            HexCoordinate position
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
        this.consecutiveMissedUpkeepTurns = 0;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public HexCoordinate getPosition() {
        return position;
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
