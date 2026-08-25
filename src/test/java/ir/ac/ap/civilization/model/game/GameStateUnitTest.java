package ir.ac.ap.civilization.model.game;

import ir.ac.ap.civilization.model.map.HexCoordinate;
import ir.ac.ap.civilization.model.map.HexTile;
import ir.ac.ap.civilization.model.unit.Unit;
import ir.ac.ap.civilization.model.unit.UnitType;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameStateUnitTest {

    @Test
    void gameStartsWithRequiredCivilianUnits() {
        GameState gameState = new GameState(123L);

        assertEquals(5, gameState.getUnits().size());
        assertEquals(
                2,
                countUnitsOfType(
                        gameState,
                        UnitType.BUILDER
                )
        );
        assertEquals(
                2,
                countUnitsOfType(
                        gameState,
                        UnitType.WORKER
                )
        );
        assertEquals(
                1,
                countUnitsOfType(
                        gameState,
                        UnitType.EXPLORER
                )
        );
    }

    @Test
    void startingUnitsHaveUniqueIdsAndPositions() {
        GameState gameState = new GameState(123L);

        Set<Integer> ids = new HashSet<>();
        Set<HexCoordinate> positions = new HashSet<>();

        for (Unit unit : gameState.getUnits()) {
            ids.add(unit.getId());
            positions.add(unit.getPosition());
        }

        assertEquals(5, ids.size());
        assertEquals(5, positions.size());
    }

    @Test
    void startingUnitsAreAdjacentToTownHallAndPassable() {
        GameState gameState = new GameState(123L);
        HexCoordinate townHallCoordinate =
                gameState.getTownHallCoordinate();

        for (Unit unit : gameState.getUnits()) {
            assertTrue(
                    townHallCoordinate.isAdjacentTo(
                            unit.getPosition()
                    )
            );

            HexTile tile = gameState.getGameMap()
                    .getTile(unit.getPosition())
                    .orElseThrow(AssertionError::new);

            assertTrue(tile.isNormallyPassable());
        }
    }

    @Test
    void unitsCanBeFoundByIdAndPosition() {
        GameState gameState = new GameState(123L);
        Unit firstUnit = gameState.getUnits().get(0);

        assertTrue(
                gameState
                        .getUnitById(firstUnit.getId())
                        .isPresent()
        );

        assertFalse(
                gameState.getUnitById(999).isPresent()
        );

        assertEquals(
                1,
                gameState
                        .getUnitsAt(firstUnit.getPosition())
                        .size()
        );
    }

    @Test
    void externalCodeCannotChangeUnitList() {
        GameState gameState = new GameState(123L);

        assertThrows(
                UnsupportedOperationException.class,
                () -> gameState.getUnits().clear()
        );

        assertEquals(5, gameState.getUnits().size());
    }

    private long countUnitsOfType(
            GameState gameState,
            UnitType unitType
    ) {
        return gameState.getUnits()
                .stream()
                .filter(
                        unit -> unit.getUnitType() == unitType
                )
                .count();
    }
}
