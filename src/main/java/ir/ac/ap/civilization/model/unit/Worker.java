package ir.ac.ap.civilization.model.unit;

import ir.ac.ap.civilization.model.map.HexCoordinate;

public final class Worker extends Unit {

    private static final int MAXIMUM_ACTION_POINTS = 4;
    private static final int VISION_RADIUS = 1;

    public Worker(int id, HexCoordinate position) {
        super(
                id,
                UnitType.WORKER,
                MAXIMUM_ACTION_POINTS,
                VISION_RADIUS,
                position
        );
    }
}