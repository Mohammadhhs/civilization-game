package ir.ac.ap.civilization.model.disaster;

import ir.ac.ap.civilization.model.map.HexCoordinate;

public final class DisasterReport {
    private final DisasterType type;
    private final HexCoordinate center;
    private final int affectedEntities;

    public DisasterReport(DisasterType type, HexCoordinate center, int affectedEntities) {
        this.type = type;
        this.center = center;
        this.affectedEntities = affectedEntities;
    }
    public DisasterType getType() { return type; }
    public HexCoordinate getCenter() { return center; }
    public int getAffectedEntities() { return affectedEntities; }
}
