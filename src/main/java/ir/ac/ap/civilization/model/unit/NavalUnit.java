package ir.ac.ap.civilization.model.unit;

import ir.ac.ap.civilization.model.entity.Owner;
import ir.ac.ap.civilization.model.map.HexCoordinate;

public final class NavalUnit extends Unit {
    public NavalUnit(int id, UnitType type, HexCoordinate position) {
        this(id, type, position, Owner.PLAYER, type.getMaximumHitPoints());
    }

    public NavalUnit(int id, UnitType type, HexCoordinate position,
                     Owner owner, int currentHitPoints) {
        super(id, requireNaval(type), type.getMaximumActionPoints(), 3,
                position, owner, currentHitPoints, type.getMaximumHitPoints());
    }

    private static UnitType requireNaval(UnitType type) {
        if (type == null || !type.isNaval()) {
            throw new IllegalArgumentException("NavalUnit requires a naval type");
        }
        return type;
    }
}
