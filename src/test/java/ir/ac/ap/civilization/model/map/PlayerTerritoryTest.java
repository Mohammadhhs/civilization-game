package ir.ac.ap.civilization.model.map;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlayerTerritoryTest {

    @Test
    void claimAreaShouldClaimCenterAndValidNeighbors() {
        GameMap gameMap =
                new HexMapGenerator().generate(3, 123L);
        PlayerTerritory territory =
                new PlayerTerritory(gameMap);
        HexCoordinate center = new HexCoordinate(0, 0);

        assertEquals(7, territory.claimArea(center, 1));
        assertEquals(7, territory.size());
        assertTrue(territory.contains(center));

        for (HexCoordinate coordinate : center.getNeighbors()) {
            assertTrue(territory.contains(coordinate));
            assertTrue(
                    gameMap.getTile(coordinate)
                            .orElseThrow(AssertionError::new)
                            .isInsidePlayerTerritory()
            );
        }
    }

    @Test
    void claimingOwnedAreaAgainShouldNotDuplicateTiles() {
        GameMap gameMap =
                new HexMapGenerator().generate(3, 123L);
        PlayerTerritory territory =
                new PlayerTerritory(gameMap);
        HexCoordinate center = new HexCoordinate(0, 0);

        assertEquals(7, territory.claimArea(center, 1));
        assertEquals(0, territory.claimArea(center, 1));
        assertEquals(7, territory.size());
    }

    @Test
    void coordinatesOutsideMapShouldBeRejected() {
        GameMap gameMap =
                new HexMapGenerator().generate(2, 123L);
        PlayerTerritory territory =
                new PlayerTerritory(gameMap);

        assertFalse(
                territory.contains(new HexCoordinate(5, 0))
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> territory.claim(
                        new HexCoordinate(5, 0)
                )
        );
    }

    @Test
    void returnedCoordinatesShouldBeUnmodifiable() {
        GameMap gameMap =
                new HexMapGenerator().generate(2, 123L);
        PlayerTerritory territory =
                new PlayerTerritory(gameMap);

        territory.claim(new HexCoordinate(0, 0));

        assertThrows(
                UnsupportedOperationException.class,
                () -> territory.getOwnedCoordinates().clear()
        );
    }
}
