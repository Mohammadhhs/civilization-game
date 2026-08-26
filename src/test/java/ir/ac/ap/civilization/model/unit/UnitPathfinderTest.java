package ir.ac.ap.civilization.model.unit;

import ir.ac.ap.civilization.model.game.GameState;
import ir.ac.ap.civilization.model.map.HexCoordinate;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class UnitPathfinderTest {

    @Test
    void findsShortestAdjacentRouteToTownHall() {
        GameState state = new GameState(140405L);
        Unit explorer = null;
        for (Unit unit : state.getUnits()) {
            if (unit.getUnitType() == UnitType.EXPLORER) explorer = unit;
        }

        List<HexCoordinate> path = new UnitPathfinder(state).findPath(
                explorer, state.getTownHallCoordinate());

        assertFalse(path.isEmpty());
        assertEquals(1, path.size());
        assertEquals(state.getTownHallCoordinate(), path.get(0));
    }
}
