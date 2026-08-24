package ir.ac.ap.civilization.model.unit;

import ir.ac.ap.civilization.model.map.HexCoordinate;

public final class Worker extends Unit {

    private static final int MAXIMUM_ACTION_POINTS = 4;
    private static final int VISION_RADIUS = 1;

    private Integer stationedBuildingId;

    public Worker(int id, HexCoordinate position) {
        super(
                id,
                UnitType.WORKER,
                MAXIMUM_ACTION_POINTS,
                VISION_RADIUS,
                position
        );

        stationedBuildingId = null;
    }

    public boolean isStationed() {
        return stationedBuildingId != null;
    }

    public Integer getStationedBuildingId() {
        return stationedBuildingId;
    }

    public void stationAt(int buildingId) {
        if (buildingId <= 0) {
            throw new IllegalArgumentException(
                    "Building id must be positive"
            );
        }

        if (isStationed()) {
            throw new IllegalStateException(
                    "Worker is already stationed"
            );
        }

        stationedBuildingId = buildingId;
    }

    public void leaveBuilding() {
        if (!isStationed()) {
            throw new IllegalStateException(
                    "Worker is not stationed"
            );
        }

        stationedBuildingId = null;
    }
}
