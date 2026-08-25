package ir.ac.ap.civilization.model.map;

import ir.ac.ap.civilization.model.building.MaintainedBuilding;
import ir.ac.ap.civilization.model.unit.Unit;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class FogOfWarService {

    public static final int TOWN_HALL_VISION_RADIUS = 2;

    public void refreshVisibility(
            GameMap gameMap,
            HexCoordinate townHallCoordinate,
            List<Unit> units
    ) {
        refreshVisibility(
                gameMap,
                townHallCoordinate,
                units,
                Collections
                        .<MaintainedBuilding>emptyList()
        );
    }

    public void refreshVisibility(
            GameMap gameMap,
            HexCoordinate townHallCoordinate,
            List<Unit> units,
            List<MaintainedBuilding> buildings
    ) {
        Objects.requireNonNull(
                gameMap,
                "Game map cannot be null"
        );

        Objects.requireNonNull(
                townHallCoordinate,
                "Town Hall coordinate cannot be null"
        );

        Objects.requireNonNull(
                units,
                "Unit list cannot be null"
        );

        Objects.requireNonNull(
                buildings,
                "Building list cannot be null"
        );

        hideAllTiles(gameMap);

        revealArea(
                gameMap,
                townHallCoordinate,
                TOWN_HALL_VISION_RADIUS
        );

        for (Unit unit : units) {
            Unit nonNullUnit = Objects.requireNonNull(
                    unit,
                    "Unit cannot be null"
            );

            revealArea(
                    gameMap,
                    nonNullUnit.getPosition(),
                    nonNullUnit.getVisionRadius()
            );
        }

        for (MaintainedBuilding building : buildings) {
            MaintainedBuilding nonNullBuilding =
                    Objects.requireNonNull(
                            building,
                            "Building cannot be null"
                    );

            revealArea(
                    gameMap,
                    nonNullBuilding.getPosition(),
                    nonNullBuilding.getVisionRadius()
            );
        }
    }

    private void hideAllTiles(GameMap gameMap) {
        for (HexTile tile : gameMap.getTiles()) {
            tile.setVisible(false);
        }
    }

    private void revealArea(
            GameMap gameMap,
            HexCoordinate center,
            int radius
    ) {
        for (HexCoordinate coordinate :
                center.getCoordinatesWithinRadius(radius)) {

            gameMap.getTile(coordinate)
                    .ifPresent(
                            tile -> tile.setVisible(true)
                    );
        }
    }
}
