package ir.ac.ap.civilization.model.unit;

import ir.ac.ap.civilization.model.map.HexCoordinate;

public final class Explorer extends Unit {

    private static final int MAXIMUM_ACTION_POINTS = 8;
    private static final int VISION_RADIUS = 3;

    public Explorer(int id, HexCoordinate position) {
        super(
                id,
                UnitType.EXPLORER,
                MAXIMUM_ACTION_POINTS,
                VISION_RADIUS,
                position
        );
    }
}