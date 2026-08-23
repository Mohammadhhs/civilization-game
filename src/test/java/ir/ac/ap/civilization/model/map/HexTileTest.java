package ir.ac.ap.civilization.model.map;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HexTileTest {

    @Test
    void constructorShouldStoreInitialInformation() {
        HexCoordinate coordinate = new HexCoordinate(2, -1);

        Set<NaturalResourceType> resources = new HashSet<>(
                Arrays.asList(
                        NaturalResourceType.STONE,
                        NaturalResourceType.IRON
                )
        );

        HexTile tile = new HexTile(
                coordinate,
                TerrainType.MOUNTAIN,
                resources
        );

        assertEquals(coordinate, tile.getCoordinate());
        assertEquals(TerrainType.MOUNTAIN, tile.getTerrainType());

        assertTrue(
                tile.hasNaturalResource(
                        NaturalResourceType.STONE
                )
        );

        assertTrue(
                tile.hasNaturalResource(
                        NaturalResourceType.IRON
                )
        );

        assertFalse(tile.isDiscovered());
        assertFalse(tile.isVisible());
        assertFalse(tile.isInsidePlayerTerritory());
    }

    @Test
    void naturalResourcesShouldBeDefensivelyCopied() {
        Set<NaturalResourceType> originalResources =
                new HashSet<>();

        originalResources.add(NaturalResourceType.STONE);

        HexTile tile = new HexTile(
                new HexCoordinate(0, 0),
                TerrainType.MOUNTAIN,
                originalResources
        );

        originalResources.add(NaturalResourceType.IRON);

        assertTrue(
                tile.hasNaturalResource(
                        NaturalResourceType.STONE
                )
        );

        assertFalse(
                tile.hasNaturalResource(
                        NaturalResourceType.IRON
                )
        );
    }

    @Test
    void returnedNaturalResourcesShouldBeUnmodifiable() {
        Set<NaturalResourceType> resources = new HashSet<>();
        resources.add(NaturalResourceType.WOOD);

        HexTile tile = new HexTile(
                new HexCoordinate(0, 0),
                TerrainType.FOREST,
                resources
        );

        assertThrows(
                UnsupportedOperationException.class,
                () -> tile.getNaturalResources().add(
                        NaturalResourceType.IRON
                )
        );
    }

    @Test
    void visibleTileShouldAutomaticallyBecomeDiscovered() {
        HexTile tile = new HexTile(
                new HexCoordinate(0, 0),
                TerrainType.GRASSLAND
        );

        tile.setVisible(true);

        assertTrue(tile.isVisible());
        assertTrue(tile.isDiscovered());

        tile.setVisible(false);

        assertFalse(tile.isVisible());
        assertTrue(tile.isDiscovered());
    }

    @Test
    void discoverShouldNotMakeTileCurrentlyVisible() {
        HexTile tile = new HexTile(
                new HexCoordinate(0, 0),
                TerrainType.PLAIN
        );

        tile.discover();

        assertTrue(tile.isDiscovered());
        assertFalse(tile.isVisible());
    }

    @Test
    void playerTerritoryStatusShouldBeChangeable() {
        HexTile tile = new HexTile(
                new HexCoordinate(0, 0),
                TerrainType.GRASSLAND
        );

        assertFalse(tile.isInsidePlayerTerritory());

        tile.setInsidePlayerTerritory(true);

        assertTrue(tile.isInsidePlayerTerritory());

        tile.setInsidePlayerTerritory(false);

        assertFalse(tile.isInsidePlayerTerritory());
    }

    @Test
    void terrainPropertiesShouldMatchMovementRules() {
        assertEquals(
                1,
                TerrainType.GRASSLAND.getBaseMovementCost()
        );

        assertTrue(
                TerrainType.GRASSLAND.isNormallyPassable()
        );

        assertTrue(
                TerrainType.GRASSLAND.isConstructionAllowed()
        );

        assertEquals(
                2,
                TerrainType.SEA.getBaseMovementCost()
        );

        assertFalse(
                TerrainType.SEA.isNormallyPassable()
        );

        assertFalse(
                TerrainType.SEA.isConstructionAllowed()
        );

        assertFalse(
                TerrainType.MOUNTAIN_RANGE.isNormallyPassable()
        );

        assertFalse(
                TerrainType.MOUNTAIN_RANGE
                        .isConstructionAllowed()
        );
    }

    @Test
    void constructorShouldRejectNullArguments() {
        HexCoordinate coordinate =
                new HexCoordinate(0, 0);

        assertThrows(
                NullPointerException.class,
                () -> new HexTile(
                        null,
                        TerrainType.PLAIN
                )
        );

        assertThrows(
                NullPointerException.class,
                () -> new HexTile(
                        coordinate,
                        null
                )
        );

        assertThrows(
                NullPointerException.class,
                () -> new HexTile(
                        coordinate,
                        TerrainType.PLAIN,
                        null
                )
        );
    }
}