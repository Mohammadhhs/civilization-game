package ir.ac.ap.civilization.model.unit;

public enum UnitType {

    BUILDER("Builder", "B"),
    WORKER("Worker", "W"),
    EXPLORER("Explorer", "E");

    private final String displayName;
    private final String mapSymbol;

    UnitType(String displayName, String mapSymbol) {
        this.displayName = displayName;
        this.mapSymbol = mapSymbol;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getMapSymbol() {
        return mapSymbol;
    }
}