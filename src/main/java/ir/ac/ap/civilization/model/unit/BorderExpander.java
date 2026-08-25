package ir.ac.ap.civilization.model.unit;

import ir.ac.ap.civilization.model.map.HexCoordinate;

public final class BorderExpander extends Unit {

    private static final int MAXIMUM_ACTION_POINTS = 5;
    private static final int VISION_RADIUS = 0;

    public BorderExpander(
            int id,
            HexCoordinate position
    ) {
        super(
                id,
                UnitType.BORDER_EXPANDER,
                MAXIMUM_ACTION_POINTS,
                VISION_RADIUS,
                position
        );
    }
}
