package ir.ac.ap.civilization.model.building;

import ir.ac.ap.civilization.model.resource.ResourceType;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

public enum TownHallLevel {

    BASE_CAMP(
            1,
            "Base Camp",
            100,
            0,
            0,
            createCost(0, 0, 0, 0)
    ),

    SETTLEMENT(
            2,
            "Settlement",
            200,
            3,
            50,
            createCost(0, 50, 50, 0)
    ),

    CAPITAL(
            3,
            "Capital",
            300,
            5,
            0,
            createCost(0, 0, 100, 50)
    );

    private final int levelNumber;
    private final String displayName;
    private final int storageCapacity;
    private final int upgradeTurns;
    private final int healAfterUpgrade;
    private final EnumMap<ResourceType, Integer> upgradeCost;

    TownHallLevel(
            int levelNumber,
            String displayName,
            int storageCapacity,
            int upgradeTurns,
            int healAfterUpgrade,
            Map<ResourceType, Integer> upgradeCost
    ) {
        this.levelNumber = levelNumber;
        this.displayName = displayName;
        this.storageCapacity = storageCapacity;
        this.upgradeTurns = upgradeTurns;
        this.healAfterUpgrade = healAfterUpgrade;
        this.upgradeCost = new EnumMap<>(upgradeCost);
    }

    public int getLevelNumber() {
        return levelNumber;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getStorageCapacity() {
        return storageCapacity;
    }

    public int getUpgradeTurns() {
        return upgradeTurns;
    }

    public int getHealAfterUpgrade() {
        return healAfterUpgrade;
    }

    public Map<ResourceType, Integer> getUpgradeCost() {
        return Collections.unmodifiableMap(
                new EnumMap<>(upgradeCost)
        );
    }

    public boolean isMaximumLevel() {
        return this == CAPITAL;
    }

    public TownHallLevel getNextLevel() {
        switch (this) {
            case BASE_CAMP:
                return SETTLEMENT;

            case SETTLEMENT:
                return CAPITAL;

            case CAPITAL:
                throw new IllegalStateException(
                        "Town Hall is already at maximum level"
                );

            default:
                throw new IllegalStateException(
                        "Unknown Town Hall level"
                );
        }
    }

    private static Map<ResourceType, Integer> createCost(
            int food,
            int wood,
            int stone,
            int iron
    ) {
        EnumMap<ResourceType, Integer> costs =
                new EnumMap<>(ResourceType.class);

        if (food > 0) {
            costs.put(ResourceType.FOOD, food);
        }

        if (wood > 0) {
            costs.put(ResourceType.WOOD, wood);
        }

        if (stone > 0) {
            costs.put(ResourceType.STONE, stone);
        }

        if (iron > 0) {
            costs.put(ResourceType.IRON, iron);
        }

        return costs;
    }
}