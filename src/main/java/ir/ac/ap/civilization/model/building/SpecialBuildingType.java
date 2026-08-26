package ir.ac.ap.civilization.model.building;

public enum SpecialBuildingType {
    BAZAAR("Bazaar", 2, 140),
    TRADING_POST("Trading Post", 1, 180),
    DOCK("Dock", 2, 160),
    MONUMENT("Monument", 2, 180);

    private final String displayName;
    private final int requiredTownHallLevel;
    private final int maximumHitPoints;

    SpecialBuildingType(
            String displayName,
            int requiredTownHallLevel,
            int maximumHitPoints
    ) {
        this.displayName = displayName;
        this.requiredTownHallLevel = requiredTownHallLevel;
        this.maximumHitPoints = maximumHitPoints;
    }

    public String getDisplayName() { return displayName; }
    public int getRequiredTownHallLevel() { return requiredTownHallLevel; }
    public int getMaximumHitPoints() { return maximumHitPoints; }
}
