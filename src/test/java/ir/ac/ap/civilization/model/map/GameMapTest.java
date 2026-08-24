package ir.ac.ap.civilization.model.map;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameMapTest {

    @Test
    void generatedMapContainsExpectedNumberOfTiles() {
        int radius = 4;

        GameMap gameMap =
                new HexMapGenerator().generate(radius, 123L);

        assertEquals(
                GameMap.calculateTileCount(radius),
                gameMap.size()
        );
    }

    @Test
    void tileCanBeRetrievedByCoordinate() {
        GameMap gameMap =
                new HexMapGenerator().generate(4, 123L);

        HexCoordinate center = new HexCoordinate(0, 0);
        HexCoordinate outside = new HexCoordinate(5, 0);

        assertTrue(gameMap.getTile(center).isPresent());
        assertFalse(gameMap.getTile(outside).isPresent());
    }

    @Test
    void centerHasSixValidNeighbors() {
        GameMap gameMap =
                new HexMapGenerator().generate(4, 123L);

        List<HexTile> neighbors =
                gameMap.getValidNeighbors(
                        new HexCoordinate(0, 0)
                );

        assertEquals(6, neighbors.size());
    }

    @Test
    void cornerTileHasOnlyThreeValidNeighbors() {
        GameMap gameMap =
                new HexMapGenerator().generate(4, 123L);

        List<HexTile> neighbors =
                gameMap.getValidNeighbors(
                        new HexCoordinate(4, 0)
                );

        assertEquals(3, neighbors.size());
    }

    @Test
    void invalidRadiusIsRejected() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new HexMapGenerator().generate(1, 123L)
        );
    }
}