package ir.ac.ap.civilization.model.unit;

import ir.ac.ap.civilization.model.resource.ResourceType;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

public enum UnitType {

    BUILDER(
            "Builder", "B", 2,
            createCost(0, 30, 10, 0)
    ),
    WORKER(
            "Worker", "W", 1,
            createCost(15, 10, 0, 0)
    ),
    EXPLORER(
            "Explorer", "E", 2,
            createCost(20, 15, 0, 0)
    ),
    BORDER_EXPANDER(
            "Border Expander", "BE", 3,
            createCost(20, 30, 20, 0)
    );

    private final String displayName;
    private final String mapSymbol;
    private final int productionTurns;
    private final Map<ResourceType, Integer> productionCost;

    UnitType(
            String displayName,
            String mapSymbol,
            int productionTurns,
            Map<ResourceType, Integer> productionCost
    ) {
        this.displayName = displayName;
        this.mapSymbol = mapSymbol;
        this.productionTurns = productionTurns;
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
