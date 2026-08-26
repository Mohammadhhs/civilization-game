package ir.ac.ap.civilization.model.unit;

import ir.ac.ap.civilization.model.entity.Owner;
import ir.ac.ap.civilization.model.map.HexCoordinate;

import java.util.Objects;

public final class MilitaryUnit extends Unit {

    public MilitaryUnit(
            int id,
            UnitType unitType,
            HexCoordinate position
    ) {
        this(id, unitType, position, Owner.PLAYER,
                unitType.getMaximumHitPoints());
    }

    public MilitaryUnit(
            int id,
            UnitType unitType,
            HexCoordinate position,
            Owner owner,
            int currentHitPoints
    ) {
        super(
                id,
                validateMilitary(unitType),
                unitType.getMaximumActionPoints(),
                2,
                position,
                owner,
                currentHitPoints,
                unitType.getMaximumHitPoints()
        );
    }

    public int getAttackRange() {
        return getUnitType().getAttackRange();
    }

    private static UnitType validateMilitary(UnitType unitType) {
        Objects.requireNonNull(unitType, "Unit type cannot be null");
        if (!unitType.isMilitary()) {
            throw new IllegalArgumentException(
                    "MilitaryUnit requires a military unit type"
            );
        }
        return unitType;
    }
}
