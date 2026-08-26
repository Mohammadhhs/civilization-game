package ir.ac.ap.civilization.model.game;

import ir.ac.ap.civilization.model.map.GameMap;
import ir.ac.ap.civilization.model.map.HexCoordinate;
import ir.ac.ap.civilization.model.map.HexTile;
import ir.ac.ap.civilization.model.map.PlayerTerritory;
import ir.ac.ap.civilization.model.unit.BorderExpander;
import ir.ac.ap.civilization.model.unit.Unit;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class BorderExpansionService {

    private static final int EXPANSION_RADIUS = 1;

    private final GameState gameState;

    public BorderExpansionService(GameState gameState) {
        this.gameState = Objects.requireNonNull(
                gameState,
                "Game state cannot be null"
        );
    }

    public boolean canExpand(int unitId) {
        return getExpansionProblem(unitId) == null;
    }

    public String getExpansionProblem(int unitId) {
        try {
            validateExpansion(unitId);
            return null;
        } catch (
                IllegalArgumentException |
                IllegalStateException exception
        ) {
            return exception.getMessage();
        }
    }

    public int expand(int unitId) {
        ExpansionContext context = validateExpansion(unitId);
        int claimedTiles = 0;

        for (HexCoordinate coordinate : context.area) {
            if (gameState.getPlayerTerritory().claim(coordinate)) {
                claimedTiles = Math.incrementExact(claimedTiles);
            }
        }

        gameState.removeUnit(context.expander);
        gameState.refreshVisibility();
        return claimedTiles;
    }

    private ExpansionContext validateExpansion(int unitId) {
        Unit unit = gameState.getUnitById(unitId)
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Unit does not exist"
                        )
                );

        if (!(unit instanceof BorderExpander)) {
            throw new IllegalArgumentException(
                    "Only a Border Expander can expand territory"
            );
        }

        GameMap gameMap = gameState.getGameMap();
        PlayerTerritory territory =
                gameState.getPlayerTerritory();

        List<HexCoordinate> area = new ArrayList<>();

        for (HexCoordinate coordinate :
                unit.getPosition()
                        .getCoordinatesWithinRadius(
                                EXPANSION_RADIUS
                        )) {
            if (gameMap.containsCoordinate(coordinate)) {
                area.add(coordinate);
            }
        }

        if (!isConnectedToTerritory(area, territory)) {
            throw new IllegalStateException(
                    "Expansion area must touch current territory"
            );
        }

        boolean containsUnownedTile = false;

        for (HexCoordinate coordinate : area) {
            HexTile tile = gameMap.getTile(coordinate)
                    .orElseThrow(
                            () -> new IllegalStateException(
                                    "Expansion tile is outside map"
                            )
                    );

            if (!territory.contains(coordinate) &&
                    !tile.isDiscovered()) {
                throw new IllegalStateException(
                        "Every new expansion hex must be explored first"
                );
            }

            if (!territory.contains(coordinate)) {
                containsUnownedTile = true;
            }
        }

        if (!containsUnownedTile) {
            throw new IllegalStateException(
                    "Expansion area is already owned"
            );
        }

        return new ExpansionContext(
                (BorderExpander) unit,
                area
        );
    }

    private boolean isConnectedToTerritory(
            List<HexCoordinate> area,
            PlayerTerritory territory
    ) {
        for (HexCoordinate coordinate : area) {
            if (territory.contains(coordinate)) {
                return true;
            }

            for (HexCoordinate neighbor :
                    coordinate.getNeighbors()) {
                if (territory.contains(neighbor)) {
                    return true;
                }
            }
        }

        return false;
    }

    private static final class ExpansionContext {

        private final BorderExpander expander;
        private final List<HexCoordinate> area;

        private ExpansionContext(
                BorderExpander expander,
                List<HexCoordinate> area
        ) {
            this.expander = expander;
            this.area = area;
        }
    }
}
