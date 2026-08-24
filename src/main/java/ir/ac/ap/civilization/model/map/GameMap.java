package ir.ac.ap.civilization.model.map;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class GameMap {

    private static final HexCoordinate CENTER =
            new HexCoordinate(0, 0);

    private final int radius;
    private final long seed;
    private final LinkedHashMap<HexCoordinate, HexTile> tiles;

    public GameMap(
            int radius,
            long seed,
            Map<HexCoordinate, HexTile> tiles
    ) {
        if (radius < 1) {
            throw new IllegalArgumentException(
                    "Map radius must be at least 1"
            );
        }

        Objects.requireNonNull(tiles, "Tiles cannot be null");

        this.radius = radius;
        this.seed = seed;
        this.tiles = new LinkedHashMap<>();

        copyAndValidateTiles(tiles);
        validateTileCount();
    }

    public int getRadius() {
        return radius;
    }

    public long getSeed() {
        return seed;
    }

    public int size() {
        return tiles.size();
    }

    public boolean containsCoordinate(HexCoordinate coordinate) {
        Objects.requireNonNull(
                coordinate,
                "Coordinate cannot be null"
        );

        return tiles.containsKey(coordinate);
    }

    public Optional<HexTile> getTile(HexCoordinate coordinate) {
        Objects.requireNonNull(
                coordinate,
                "Coordinate cannot be null"
        );

        return Optional.ofNullable(tiles.get(coordinate));
    }

    public Collection<HexTile> getTiles() {
        return Collections.unmodifiableCollection(
                tiles.values()
        );
    }

    public List<HexTile> getValidNeighbors(
            HexCoordinate coordinate
    ) {
        Objects.requireNonNull(
                coordinate,
                "Coordinate cannot be null"
        );

        if (!containsCoordinate(coordinate)) {
            return Collections.emptyList();
        }

        List<HexTile> neighbors = new ArrayList<>(6);

        for (HexCoordinate neighborCoordinate :
                coordinate.getNeighbors()) {

            HexTile neighbor = tiles.get(neighborCoordinate);

            if (neighbor != null) {
                neighbors.add(neighbor);
            }
        }

        return Collections.unmodifiableList(neighbors);
    }

    public static int calculateTileCount(int radius) {
        if (radius < 0) {
            throw new IllegalArgumentException(
                    "Map radius cannot be negative"
            );
        }

        return Math.addExact(
                1,
                Math.multiplyExact(
                        3,
                        Math.multiplyExact(radius, radius + 1)
                )
        );
    }

    private void copyAndValidateTiles(
            Map<HexCoordinate, HexTile> sourceTiles
    ) {
        for (Map.Entry<HexCoordinate, HexTile> entry :
                sourceTiles.entrySet()) {

            HexCoordinate coordinate = Objects.requireNonNull(
                    entry.getKey(),
                    "Tile coordinate cannot be null"
            );

            HexTile tile = Objects.requireNonNull(
                    entry.getValue(),
                    "Tile cannot be null"
            );

            if (!coordinate.equals(tile.getCoordinate())) {
                throw new IllegalArgumentException(
                        "Tile coordinate does not match its map key"
                );
            }

            if (CENTER.distanceTo(coordinate) > radius) {
                throw new IllegalArgumentException(
                        "Tile is outside the map radius: " +
                                coordinate
                );
            }

            this.tiles.put(coordinate, tile);
        }
    }

    private void validateTileCount() {
        int expectedTileCount = calculateTileCount(radius);

        if (tiles.size() != expectedTileCount) {
            throw new IllegalArgumentException(
                    "Incomplete hex map. Expected " +
                            expectedTileCount +
                            " tiles but received " +
                            tiles.size()
            );
        }
    }
}