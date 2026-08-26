package ir.ac.ap.civilization.model.map;

import java.io.Serializable;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/** A serializable, UI-independent draft used by the map editor. */
public final class CustomMapDesign implements Serializable {

    private static final long serialVersionUID = 1L;

    private final int radius;
    private final long seed;
    private final LinkedHashMap<HexCoordinate, TerrainType> terrain;
    private final LinkedHashMap<HexCoordinate, NaturalResourceType> resources;

    public CustomMapDesign(int radius, long seed) {
        if (radius < 2 || radius > 12) {
            throw new IllegalArgumentException("Editor radius must be between 2 and 12");
        }
        this.radius = radius;
        this.seed = seed;
        this.terrain = new LinkedHashMap<>();
        this.resources = new LinkedHashMap<>();

        GameMap generated = new HexMapGenerator().generate(radius, seed);
        for (HexTile tile : generated.getTiles()) {
            terrain.put(tile.getCoordinate(), tile.getTerrainType());
            if (!tile.getNaturalResources().isEmpty()) {
                resources.put(tile.getCoordinate(),
                        tile.getNaturalResources().iterator().next());
            }
        }
        terrain.put(new HexCoordinate(0, 0), TerrainType.PLAIN);
        resources.remove(new HexCoordinate(0, 0));
    }

    public int getRadius() { return radius; }
    public long getSeed() { return seed; }

    public Set<HexCoordinate> getCoordinates() {
        return Collections.unmodifiableSet(terrain.keySet());
    }

    public TerrainType getTerrain(HexCoordinate coordinate) {
        TerrainType value = terrain.get(coordinate);
        if (value == null) throw new IllegalArgumentException("Coordinate is outside the map");
        return value;
    }

    public NaturalResourceType getResource(HexCoordinate coordinate) {
        return resources.get(coordinate);
    }

    public void paint(
            HexCoordinate coordinate,
            TerrainType terrainType,
            NaturalResourceType resourceType
    ) {
        Objects.requireNonNull(coordinate, "Coordinate cannot be null");
        Objects.requireNonNull(terrainType, "Terrain cannot be null");
        if (!terrain.containsKey(coordinate)) {
            throw new IllegalArgumentException("Coordinate is outside the map");
        }
        if (new HexCoordinate(0, 0).distanceTo(coordinate) <= 1 &&
                !terrainType.isConstructionAllowed()) {
            throw new IllegalArgumentException("Town Hall and starting unit hexes must be passable");
        }
        terrain.put(coordinate, terrainType);
        if (resourceType == null) resources.remove(coordinate);
        else resources.put(coordinate, resourceType);
    }

    public GameMap toGameMap() {
        LinkedHashMap<HexCoordinate, HexTile> tiles = new LinkedHashMap<>();
        for (Map.Entry<HexCoordinate, TerrainType> entry : terrain.entrySet()) {
            NaturalResourceType resource = resources.get(entry.getKey());
            Set<NaturalResourceType> tileResources = resource == null
                    ? Collections.<NaturalResourceType>emptySet()
                    : EnumSet.of(resource);
            tiles.put(entry.getKey(), new HexTile(
                    entry.getKey(), entry.getValue(), tileResources
            ));
        }
        return new GameMap(radius, seed, tiles);
    }
}
