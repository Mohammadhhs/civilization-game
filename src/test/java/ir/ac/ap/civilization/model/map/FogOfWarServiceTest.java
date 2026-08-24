package ir.ac.ap.civilization.model.map;

import ir.ac.ap.civilization.model.game.GameState;
import ir.ac.ap.civilization.model.unit.Explorer;
import ir.ac.ap.civilization.model.unit.Unit;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FogOfWarServiceTest {

    @Test
    void initialGameShouldRevealOnlyTownHallAndUnitVision() {
        GameState gameState = new GameState();

        HexTile townHallTile = gameState.getGameMap()
                .getTile(
                        gameState.getTownHallCoordinate()
                )
                .orElseThrow(AssertionError::new);

        HexTile farTile = gameState.getGameMap()
                .getTile(new HexCoordinate(7, 0))
                .orElseThrow(AssertionError::new);

        assertTrue(townHallTile.isVisible());
        assertTrue(townHallTile.isDiscovered());

        assertFalse(farTile.isVisible());
        assertFalse(farTile.isDiscovered());
    }

    @Test
    void discoveredTileShouldStayDiscoveredAfterLeavingVision() {
        GameMap gameMap =
                new HexMapGenerator().generate(1234L);

        FogOfWarService fogOfWarService =
                new FogOfWarService();

        HexCoordinate townHall =
                new HexCoordinate(0, 0);

        Unit firstExplorer = new Explorer(
                1,
                new HexCoordinate(0, 0)
        );

        fogOfWarService.refreshVisibility(
                gameMap,
                townHall,
                Collections.singletonList(firstExplorer)
        );

        HexTile oldVisionTile = gameMap
                .getTile(new HexCoordinate(3, 0))
                .orElseThrow(AssertionError::new);

        assertTrue(oldVisionTile.isVisible());
        assertTrue(oldVisionTile.isDiscovered());

        Unit movedExplorer = new Explorer(
                1,
                new HexCoordinate(-4, 0)
        );

        fogOfWarService.refreshVisibility(
                gameMap,
                townHall,
                Collections.singletonList(movedExplorer)
        );

        HexTile newVisionTile = gameMap
                .getTile(new HexCoordinate(-7, 0))
                .orElseThrow(AssertionError::new);

        assertFalse(oldVisionTile.isVisible());
        assertTrue(oldVisionTile.isDiscovered());

        assertTrue(newVisionTile.isVisible());
        assertTrue(newVisionTile.isDiscovered());
    }
}