package ir.ac.ap.civilization.model.unit;

import ir.ac.ap.civilization.model.resource.ResourceType;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

public enum UnitType {

    BUILDER(
            "Builder", "B", 2, false, 100, 5, 0,
            createCost(0, 30, 10, 0)
    ),
    WORKER(
            "Worker", "W", 1, false, 100, 4, 0,
            createCost(15, 10, 0, 0)
    ),
    EXPLORER(
            "Explorer", "E", 2, false, 100, 6, 0,
            createCost(20, 15, 0, 0)
    ),
    BORDER_EXPANDER(
            "Border Expander", "BE", 3, false, 100, 4, 0,
            createCost(20, 30, 20, 0)
    ),
    SWORDSMAN(
            "Swordsman", "SW", 2, true, 100, 3, 1,
            createCost(20, 40, 0, 0)
    ),
    ARCHER(
            "Archer", "AR", 3, true, 70, 3, 2,
            createCost(30, 30, 0, 10)
    ),
    CAVALRY(
            "Cavalry", "CV", 3, true, 80, 5, 1,
            createCost(40, 20, 0, 20)
    ),
    CATAPULT(
            "Catapult", "CT", 4, true, 50, 2, 2,
            createCost(20, 40, 30, 20)
    ),
    FISHING_BOAT(
            "Fishing Boat", "FB", 3, false, 80, 4, 0,
            createCost(20, 50, 0, 0)
    ),
    GALLEY(
            "Galley", "GL", 4, true, 130, 4, 2,
            createCost(30, 70, 0, 30)
    ),
    PIRATE(
            "Pirate", "PR", 4, true, 110, 4, 2,
            createCost(0, 0, 0, 0)
    );

    private final String displayName;
    private final String mapSymbol;
    private final int productionTurns;
    private final boolean military;
    private final int maximumHitPoints;
    private final int maximumActionPoints;
    private final int attackRange;
    private final Map<ResourceType, Integer> productionCost;

    UnitType(
            String displayName,
            String mapSymbol,
            int productionTurns,
            boolean military,
            int maximumHitPoints,
            int maximumActionPoints,
            int attackRange,
            Map<ResourceType, Integer> productionCost
    ) {
        this.displayName = displayName;
        this.mapSymbol = mapSymbol;
        this.productionTurns = productionTurns;
        this.military = military;
        this.maximumHitPoints = maximumHitPoints;
        this.maximumActionPoints = maximumActionPoints;
        this.attackRange = attackRange;
        this.productionCost = productionCost;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getMapSymbol() {
        return mapSymbol;
    }

    public int getProductionTurns() {
        return productionTurns;
    }

    public Map<ResourceType, Integer> getProductionCost() {
        return productionCost;
    }

    public boolean isMilitary() {
        return military;
    }

    public int getMaximumHitPoints() {
        return maximumHitPoints;
    }

    public int getMaximumActionPoints() {
        return maximumActionPoints;
    }

    public int getAttackRange() {
        return attackRange;
    }

    public boolean isNaval() {
        return this == FISHING_BOAT || this == GALLEY || this == PIRATE;
    }

    @Override
    public String toString() {
        return displayName;
    }

    private static Map<ResourceType, Integer> createCost(
            int food,
            int wood,
            int stone,
            int iron
    ) {
        EnumMap<ResourceType, Integer> cost =
                new EnumMap<>(ResourceType.class);

        putPositive(cost, ResourceType.FOOD, food);
        putPositive(cost, ResourceType.WOOD, wood);
        putPositive(cost, ResourceType.STONE, stone);
        putPositive(cost, ResourceType.IRON, iron);

        return Collections.unmodifiableMap(cost);
    }

    private static void putPositive(
            Map<ResourceType, Integer> cost,
            ResourceType resourceType,
            int amount
    ) {
        if (amount > 0) {
            cost.put(resourceType, amount);
        }
    }
}
