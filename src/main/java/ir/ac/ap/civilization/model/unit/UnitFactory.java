package ir.ac.ap.civilization.model.unit;

import ir.ac.ap.civilization.model.map.HexCoordinate;

import java.util.Objects;
import java.io.Serializable;

public final class UnitFactory implements Serializable {

    public Unit create(
            int id,
            UnitType unitType,
            HexCoordinate position
    ) {
        Objects.requireNonNull(
                unitType,
                "Unit type cannot be null"
        );

        switch (unitType) {
            case BUILDER:
                return new Builder(id, position);

            case WORKER:
                return new Worker(id, position);

            case EXPLORER:
                return new Explorer(id, position);

            case BORDER_EXPANDER:
                return new BorderExpander(id, position);

            case SWORDSMAN:
            case ARCHER:
            case CAVALRY:
            case CATAPULT:
                return new MilitaryUnit(id, unitType, position);

            case FISHING_BOAT:
            case GALLEY:
            case PIRATE:
                return new NavalUnit(id, unitType, position);

            default:
                throw new IllegalStateException(
                        "Unknown unit type"
                );
        }
    }
}
