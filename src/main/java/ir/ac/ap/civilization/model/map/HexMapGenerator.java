package ir.ac.ap.civilization.model.map;

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public final class HexMapGenerator {

    public static final int DEFAULT_RADIUS = 7;
    public static final long DEFAULT_SEED = 140405L;

    private static final int SAFE_START_RADIUS = 2;
    private static final int SMOOTHING_PASSES = 2;
    private static final long DEPOSIT_SEED_SALT =
            0x5DEECE66DL;

    private static final HexCoordinate CENTER =
            new HexCoordinate(0, 0);

    private static final HexCoordinate STARTING_FOREST =
            new HexCoordinate(1, 0);

    public GameMap generate(long seed) {
        return generate(DEFAULT_RADIUS, seed);
    }

    public GameMap generate(int radius, long seed) {
        if (radius < SAFE_START_RADIUS) {
            throw new IllegalArgumentException(
                    "Map radius must be at least " +
                            SAFE_START_RADIUS
            );
        }

        Random random = new Random(seed);
        Random depositRandom = new Random(
                seed ^ DEPOSIT_SEED_SALT
        );

        List<HexCoordinate> coordinates =
                CENTER.getCoordinatesWithinRadius(radius);

        LinkedHashMap<HexCoordinate, TerrainType> terrainByCoordinate =
                createInitialTerrain(coordinates, random);

        smoothTerrain(
                coordinates,
                terrainByCoordinate,
                random
        );

        createSafeStartingArea(terrainByCoordinate);

        LinkedHashMap<HexCoordinate, HexTile> tiles =
                createTiles(
                        coordinates,
                        terrainByCoordinate,
                        random,
                        depositRandom
                );

        return new GameMap(radius, seed, tiles);
    }

    private LinkedHashMap<HexCoordinate, TerrainType>
    createInitialTerrain(
            List<HexCoordinate> coordinates,
            Random random
    ) {
        LinkedHashMap<HexCoordinate, TerrainType> result =
                new LinkedHashMap<>();

        for (HexCoordinate coordinate : coordinates) {
            result.put(
                    coordinate,
                    chooseWeightedTerrain(random)
            );
        }

        return result;
    }

    private TerrainType chooseWeightedTerrain(Random random) {
        int roll = random.nextInt(100);

        if (roll < 25) {
            return TerrainType.FOREST;
        }

        if (roll < 50) {
            return TerrainType.PLAIN;
        }

        if (roll < 73) {
            return TerrainType.GRASSLAND;
        }

        if (roll < 88) {
            return TerrainType.MOUNTAIN;
        }

        if (roll < 96) {
            return TerrainType.SEA;
        }

        return TerrainType.MOUNTAIN_RANGE;
    }

    private void smoothTerrain(
            List<HexCoordinate> coordinates,
            LinkedHashMap<HexCoordinate, TerrainType>
                    terrainByCoordinate,
            Random random
    ) {
        for (int pass = 0; pass < SMOOTHING_PASSES; pass++) {
            LinkedHashMap<HexCoordinate, TerrainType> snapshot =
                    new LinkedHashMap<>(terrainByCoordinate);

            for (HexCoordinate coordinate : coordinates) {
                TerrainType dominantTerrain =
                        findDominantNeighborTerrain(
                                coordinate,
                                snapshot
                        );

                if (dominantTerrain != null &&
                        random.nextDouble() < 0.60) {

                    terrainByCoordinate.put(
                            coordinate,
                            dominantTerrain
                    );
                }
            }
        }
    }

    private TerrainType findDominantNeighborTerrain(
            HexCoordinate coordinate,
            Map<HexCoordinate, TerrainType> terrainByCoordinate
    ) {
        EnumMap<TerrainType, Integer> counts =
                new EnumMap<>(TerrainType.class);

        for (HexCoordinate neighbor : coordinate.getNeighbors()) {
            TerrainType terrain = terrainByCoordinate.get(neighbor);

            if (terrain != null) {
                Integer currentCount = counts.get(terrain);

                counts.put(
                        terrain,
                        currentCount == null
                                ? 1
                                : currentCount + 1
                );
            }
        }

        TerrainType dominantTerrain = null;
        int highestCount = 0;

        for (TerrainType terrain : TerrainType.values()) {
            Integer count = counts.get(terrain);

            if (count != null && count > highestCount) {
                dominantTerrain = terrain;
                highestCount = count;
            }
        }

        return highestCount >= 3
                ? dominantTerrain
                : null;
    }

    private void createSafeStartingArea(
            Map<HexCoordinate, TerrainType> terrainByCoordinate
    ) {
        for (HexCoordinate coordinate :
                CENTER.getCoordinatesWithinRadius(
                        SAFE_START_RADIUS
                )) {

            TerrainType currentTerrain =
                    terrainByCoordinate.get(coordinate);

            if (currentTerrain == TerrainType.SEA ||
                    currentTerrain ==
                            TerrainType.MOUNTAIN_RANGE) {

                terrainByCoordinate.put(
                        coordinate,
                        chooseSafeTerrain(coordinate)
                );
            }
        }

        terrainByCoordinate.put(CENTER, TerrainType.PLAIN);
        terrainByCoordinate.put(
                STARTING_FOREST,
                TerrainType.FOREST
        );
    }

    private TerrainType chooseSafeTerrain(
            HexCoordinate coordinate
    ) {
        int parity = Math.floorMod(
                coordinate.getQ() + coordinate.getR(),
                2
        );

        return parity == 0
                ? TerrainType.PLAIN
                : TerrainType.GRASSLAND;
    }

    private LinkedHashMap<HexCoordinate, HexTile> createTiles(
            List<HexCoordinate> coordinates,
            Map<HexCoordinate, TerrainType> terrainByCoordinate,
            Random random,
            Random depositRandom
    ) {
        LinkedHashMap<HexCoordinate, HexTile> tiles =
                new LinkedHashMap<>();

        for (HexCoordinate coordinate : coordinates) {
            TerrainType terrain =
                    terrainByCoordinate.get(coordinate);

            Set<NaturalResourceType> resources =
                    coordinate.equals(CENTER)
                            ? Collections
                                    .<NaturalResourceType>emptySet()
                            : createNaturalResources(
                                    terrain,
                                    random
                            );

            Map<NaturalResourceType, Integer>
                    resourceAmounts =
                    createResourceAmounts(
                            resources,
                            depositRandom
                    );

            tiles.put(
                    coordinate,
                    HexTile.withResourceAmounts(
                            coordinate,
                            terrain,
                            resourceAmounts
                    )
            );
        }

        return tiles;
    }

    private Set<NaturalResourceType> createNaturalResources(
            TerrainType terrain,
            Random random
    ) {
        EnumSet<NaturalResourceType> resources =
                EnumSet.noneOf(NaturalResourceType.class);

        switch (terrain) {
            case FOREST:
                resources.add(NaturalResourceType.WOOD);
                break;

            case MOUNTAIN:
                resources.add(NaturalResourceType.STONE);

                if (random.nextDouble() < 0.30) {
                    resources.add(NaturalResourceType.IRON);
                }
                break;

            case GRASSLAND:
                if (random.nextDouble() < 0.45) {
                    resources.add(
                            random.nextBoolean()
                                    ? NaturalResourceType.WHEAT
                                    : NaturalResourceType.RICE
                    );
                }
                break;

            case PLAIN:
                if (random.nextDouble() < 0.30) {
                    resources.add(
                            random.nextBoolean()
                                    ? NaturalResourceType.CATTLE
                                    : NaturalResourceType.SHEEP
                    );
                }
                break;

            case SEA:
                if (random.nextDouble() < 0.40) {
                    resources.add(NaturalResourceType.FISH);
                }
                break;

            case MOUNTAIN_RANGE:
                break;

            default:
                throw new IllegalStateException(
                        "Unknown terrain type"
                );
        }

        return resources;
    }

    private Map<NaturalResourceType, Integer>
    createResourceAmounts(
            Set<NaturalResourceType> resources,
            Random random
    ) {
        EnumMap<NaturalResourceType, Integer> amounts =
                new EnumMap<>(NaturalResourceType.class);

        for (NaturalResourceType resource : resources) {
            amounts.put(
                    resource,
                    createResourceAmount(resource, random)
            );
        }

        return amounts;
    }

    private int createResourceAmount(
            NaturalResourceType resource,
            Random random
    ) {
        int minimum;
        int maximum;

        switch (resource) {
            case WOOD:
                minimum = 120;
                maximum = 180;
                break;

            case STONE:
                minimum = 90;
                maximum = 140;
                break;

            case IRON:
                minimum = 45;
                maximum = 75;
                break;

            case WHEAT:
            case RICE:
                minimum = 100;
                maximum = 160;
                break;

            case CATTLE:
            case SHEEP:
                minimum = 80;
                maximum = 130;
                break;

            case FISH:
                minimum = 100;
                maximum = 160;
                break;

            default:
                throw new IllegalStateException(
                        "Unknown natural resource"
                );
        }

        return minimum + random.nextInt(
                maximum - minimum + 1
        );
    }
}
