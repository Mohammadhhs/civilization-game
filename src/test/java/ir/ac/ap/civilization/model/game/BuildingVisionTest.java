package ir.ac.ap.civilization.model.game;

import ir.ac.ap.civilization.model.building.BuildingType;
import ir.ac.ap.civilization.model.map.GameMap;
import ir.ac.ap.civilization.model.map.HexCoordinate;
import ir.ac.ap.civilization.model.map.HexTile;
import ir.ac.ap.civilization.model.map.NaturalResourceType;
import ir.ac.ap.civilization.model.map.TerrainType;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BuildingVisionTest {

    private static final HexCoordinate CENTER =
            new HexCoordinate(0, 0);

    @Test
    void resourceBuildingShouldRevealItsNeighboringHexes() {
        GameState gameState = createGameState();
        HexCoordinate buildingPosition =
                new HexCoordinate(4, 0);
        HexCoordinate observedPosition =
                new HexCoordinate(5, 0);

        assertFalse(getTile(gameState, observedPosition).isVisible());

        gameState.addResourceBuilding(
                BuildingType.LUMBER_MILL,
                buildingPosition,
                NaturalResourceType.WOOD
        );
        gameState.refreshVisibility();

        assertTrue(getTile(gameState, observedPosition).isVisible());
    }

    @Test
    void settlementShouldRevealRadiusTwo() {
        GameState gameState = createGameState();
        HexCoordinate settlementPosition =
                new HexCoordinate(3, 0);
        HexCoordinate observedPosition =
                new HexCoordinate(5, 0);

        assertFalse(getTile(gameState, observedPosition).isVisible());

        gameState.addSettlement(settlementPosition);
        gameState.refreshVisibility();

        assertTrue(getTile(gameState, observedPosition).isVisible());
    }

    private HexTile getTile(
            GameState gameState,
            HexCoordinate coordinate
    ) {
        return gameState.getGameMap().getTile(coordinate)
                .orElseThrow(AssertionError::new);
    }

    private GameState createGameState() {
        Map<HexCoordinate, HexTile> tiles =
                new LinkedHashMap<>();

        for (HexCoordinate coordinate :
                CENTER.getCoordinatesWithinRadius(5)) {
            if (coordinate.equals(new HexCoordinate(4, 0))) {
                tiles.put(
                        coordinate,
                        HexTile.withResourceAmounts(
                                coordinate,
                                TerrainType.FOREST,
                                Collections.singletonMap(
                                        NaturalResourceType.WOOD,
                                        100
                                )
                        )
                );
            } else {
                tiles.put(
                        coordinate,
                        new HexTile(
                                coordinate,
                                TerrainType.PLAIN
                        )
                );
            }
        }

        return new GameState(new GameMap(5, 471L, tiles));
    }
}
