package ir.ac.ap.civilization.model.map;

import java.io.Serializable;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

public final class PlayerTerritory implements Serializable {

    private final GameMap gameMap;
    private final Set<HexCoordinate> ownedCoordinates;

    public PlayerTerritory(GameMap gameMap) {
        this.gameMap = Objects.requireNonNull(
                gameMap,
                "Game map cannot be null"
        );
        this.ownedCoordinates = new LinkedHashSet<>();
    }

    public boolean contains(HexCoordinate coordinate) {
        Objects.requireNonNull(
                coordinate,
                "Coordinate cannot be null"
        );

        return ownedCoordinates.contains(coordinate);
    }

    public int size() {
        return ownedCoordinates.size();
    }

    public Set<HexCoordinate> getOwnedCoordinates() {
        return Collections.unmodifiableSet(
                ownedCoordinates
        );
    }

    public boolean claim(HexCoordinate coordinate) {
        Objects.requireNonNull(
                coordinate,
                "Coordinate cannot be null"
        );

        HexTile tile = gameMap.getTile(coordinate)
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Territory coordinate is outside the map"
                        )
                );

        boolean added = ownedCoordinates.add(coordinate);
        tile.setInsidePlayerTerritory(true);

        return added;
    }

    public int claimArea(
            HexCoordinate center,
            int radius
    ) {
        Objects.requireNonNull(
                center,
                "Territory center cannot be null"
        );

        if (radius < 0) {
            throw new IllegalArgumentException(
                    "Territory radius cannot be negative"
            );
        }

        if (!gameMap.containsCoordinate(center)) {
            throw new IllegalArgumentException(
                    "Territory center is outside the map"
            );
        }

        int claimedTileCount = 0;

        for (HexCoordinate coordinate :
                center.getCoordinatesWithinRadius(radius)) {

            if (gameMap.containsCoordinate(coordinate) &&
                    claim(coordinate)) {

                claimedTileCount++;
            }
        }

        return claimedTileCount;
    }
}
