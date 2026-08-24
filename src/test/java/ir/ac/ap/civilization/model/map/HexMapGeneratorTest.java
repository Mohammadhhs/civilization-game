package ir.ac.ap.civilization.model.map;

import ir.ac.ap.civilization.model.game.GameState;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HexMapGeneratorTest {

    @Test
    void sameSeedCreatesSameMap() {
        HexMapGenerator generator = new HexMapGenerator();

        GameMap firstMap = generator.generate(7, 987654L);
        GameMap secondMap = generator.generate(7, 987654L);

        for (HexTile firstTile : firstMap.getTiles()) {
            HexTile secondTile = secondMap
                    .getTile(firstTile.getCoordinate())
                    .orElseThrow(AssertionError::new);

            assertEquals(
                    firstTile.getTerrainType(),
                    secondTile.getTerrainType()
            );

            assertEquals(
                    firstTile.getNaturalResources(),
                    secondTile.getNaturalResources()
            );
        }
    }

    @Test
    void differentSeedsCreateDifferentMaps() {
        HexMapGenerator generator = new HexMapGenerator();

        GameMap firstMap = generator.generate(7, 111L);
        GameMap secondMap = generator.generate(7, 222L);

        boolean differenceFound = false;

        for (HexTile firstTile : firstMap.getTiles()) {
            HexTile secondTile = secondMap
                    .getTile(firstTile.getCoordinate())
                    .orElseThrow(AssertionError::new);

            if (firstTile.getTerrainType() !=
                    secondTile.getTerrainType() ||
                    !firstTile.getNaturalResources().equals(
                            secondTile.getNaturalResources()
                    )) {

                differenceFound = true;
                break;
            }
        }

        assertTrue(differenceFound);
    }

    @Test
    void startingAreaIsPassableAndContainsNearbyWood() {
        GameMap gameMap =
                new HexMapGenerator().generate(7, 456L);

        HexCoordinate center = new HexCoordinate(0, 0);
        boolean nearbyWoodFound = false;

        for (HexCoordinate coordinate :
                center.getCoordinatesWithinRadius(2)) {

            HexTile tile = gameMap
                    .getTile(coordinate)
                    .orElseThrow(AssertionError::new);

            assertTrue(tile.isNormallyPassable());

            if (tile.hasNaturalResource(
                    NaturalResourceType.WOOD
            )) {
                nearbyWoodFound = true;
            }
        }

        assertTrue(nearbyWoodFound);
    }

    @Test
    void naturalResourcesMatchTheirTerrain() {
        GameMap gameMap =
                new HexMapGenerator().generate(7, 789L);

        for (HexTile tile : gameMap.getTiles()) {
            assertResourcesMatchTerrain(tile);
        }
    }

    @Test
    void townHallStartsOnResourceFreeCenterTile() {
        GameState gameState = new GameState(321L);

        assertEquals(
                new HexCoordinate(0, 0),
                gameState.getTownHallCoordinate()
        );

        HexTile townHallTile = gameState.getGameMap()
                .getTile(gameState.getTownHallCoordinate())
                .orElseThrow(AssertionError::new);

        assertEquals(
                TerrainType.PLAIN,
                townHallTile.getTerrainType()
        );

        assertTrue(
                townHallTile.getNaturalResources().isEmpty()
        );
    }

    private void assertResourcesMatchTerrain(HexTile tile) {
        Set<NaturalResourceType> resources =
                tile.getNaturalResources();

        switch (tile.getTerrainType()) {
            case FOREST:
                assertEquals(
                        1,
                        resources.size()
                );

                assertTrue(
                        resources.contains(
                                NaturalResourceType.WOOD
                        )
                );
                break;

            case MOUNTAIN:
                assertTrue(
                        resources.equals(
                                EnumSet.of(
                                        NaturalResourceType.STONE
                                )
                        ) ||
                                resources.equals(
                                        EnumSet.of(
                                                NaturalResourceType.STONE,
                                                NaturalResourceType.IRON
                                        )
                                )
                );
                break;

            case GRASSLAND:
                assertTrue(
                        resources.equals(
                                Collections
                                        .<NaturalResourceType>emptySet()
                        ) ||
                                resources.equals(
                                        EnumSet.of(
                                                NaturalResourceType.WHEAT
                                        )
                                ) ||
                                resources.equals(
                                        EnumSet.of(
                                                NaturalResourceType.RICE
                                        )
                                )
                );
                break;

            case PLAIN:
                assertTrue(
                        resources.equals(
                                Collections
                                        .<NaturalResourceType>emptySet()
                        ) ||
                                resources.equals(
                                        EnumSet.of(
                                                NaturalResourceType.CATTLE
                                        )
                                ) ||
                                resources.equals(
                                        EnumSet.of(
                                                NaturalResourceType.SHEEP
                                        )
                                )
                );
                break;

            case SEA:
                assertTrue(
                        resources.equals(
                                Collections
                                        .<NaturalResourceType>emptySet()
                        ) ||
                                resources.equals(
                                        EnumSet.of(
                                                NaturalResourceType.FISH
                                        )
                                )
                );
                break;

            case MOUNTAIN_RANGE:
                assertTrue(resources.isEmpty());
                break;

            default:
                throw new AssertionError(
                        "Unknown terrain type"
                );
        }
    }
}