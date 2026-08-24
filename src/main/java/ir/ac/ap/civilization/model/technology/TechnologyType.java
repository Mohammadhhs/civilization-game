package ir.ac.ap.civilization.model.technology;

import ir.ac.ap.civilization.model.resource.ResourceType;

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public enum TechnologyType {

    STORAGE_UPGRADE_I(
            "Storage Upgrade I",
            2,
            createCost(0, 30, 20, 0),
            50
    ),

    STORAGE_UPGRADE_II(
            "Storage Upgrade II",
            3,
            createCost(0, 50, 40, 0),
            50
    ),

    STONE_MINING(
            "Stone Mining",
            2,
            createCost(0, 30, 0, 0),
            0
    ),

    IRON_MINING(
            "Iron Mining",
            3,
            createCost(0, 40, 20, 0),
            0
    ),

    PROFESSIONAL_TOOLS(
            "Professional Tools",
            3,
            createCost(0, 30, 30, 20),
            0
    ),

    SETTLEMENT_CONSTRUCTION(
            "Settlement Construction",
            4,
            createCost(0, 40, 40, 20),
            0
    );

    private final String displayName;
    private final int researchTurns;
    private final Map<ResourceType, Integer> researchCost;
    private final int storageCapacityBonus;

    TechnologyType(
            String displayName,
            int researchTurns,
            Map<ResourceType, Integer> researchCost,
            int storageCapacityBonus
    ) {
        this.displayName = displayName;
        this.researchTurns = researchTurns;
        this.researchCost = researchCost;
        this.storageCapacityBonus = storageCapacityBonus;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getResearchTurns() {
        return researchTurns;
    }

    public Map<ResourceType, Integer> getResearchCost() {
        return researchCost;
    }

    public int getStorageCapacityBonus() {
        return storageCapacityBonus;
    }

    public Set<TechnologyType> getPrerequisites() {
        switch (this) {
            case STORAGE_UPGRADE_II:
                return immutableSet(STORAGE_UPGRADE_I);

            case IRON_MINING:
                return immutableSet(STONE_MINING);

            case PROFESSIONAL_TOOLS:
                return immutableSet(
                        STONE_MINING,
                        IRON_MINING
                );

            case STORAGE_UPGRADE_I:
            case STONE_MINING:
            case SETTLEMENT_CONSTRUCTION:
                return Collections.emptySet();

            default:
                throw new IllegalStateException(
                        "Unknown technology type"
                );
        }
    }

    private static Map<ResourceType, Integer> createCost(
            int food,
            int wood,
            int stone,
            int iron
    ) {
        EnumMap<ResourceType, Integer> cost =
                new EnumMap<>(ResourceType.class);

        if (food > 0) {
            cost.put(ResourceType.FOOD, food);
        }

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

    private static Set<TechnologyType> immutableSet(
            TechnologyType first
    ) {
        return Collections.unmodifiableSet(
                EnumSet.of(first)
        );
    }

    private static Set<TechnologyType> immutableSet(
            TechnologyType first,
            TechnologyType second
    ) {
        return Collections.unmodifiableSet(
                EnumSet.of(first, second)
        );
    }
}