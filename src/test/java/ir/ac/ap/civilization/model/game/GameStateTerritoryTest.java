package ir.ac.ap.civilization.model.game;

import ir.ac.ap.civilization.model.map.HexCoordinate;
import ir.ac.ap.civilization.model.map.HexTile;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameStateTerritoryTest {

    @Test
    void gameShouldStartWithRadiusTwoTerritory() {
        GameState gameState = new GameState(123L);

        HexCoordinate center =
                gameState.getTownHallCoordinate();

        assertEquals(
                19,
                gameState.getPlayerTerritory().size()
        );

        for (HexCoordinate coordinate :
                center.getCoordinatesWithinRadius(2)) {

            HexTile tile = gameState.getGameMap()
                    .getTile(coordinate)
                    .orElseThrow(AssertionError::new);

            assertTrue(
                    gameState.getPlayerTerritory()
                            .contains(coordinate)
            );

            assertTrue(
                    tile.isInsidePlayerTerritory()
            );
        }

        HexCoordinate outsideCoordinate =
                new HexCoordinate(3, 0);

        assertFalse(
                gameState.getPlayerTerritory()
                        .contains(outsideCoordinate)
        );

        assertFalse(
                gameState.getGameMap()
                        .getTile(outsideCoordinate)
                        .orElseThrow(AssertionError::new)
                        .isInsidePlayerTerritory()
        );
    }
}