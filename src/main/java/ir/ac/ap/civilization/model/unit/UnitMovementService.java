package ir.ac.ap.civilization.model.unit;

import ir.ac.ap.civilization.model.game.GameState;
import ir.ac.ap.civilization.model.map.GameMap;
import ir.ac.ap.civilization.model.map.HexCoordinate;
import ir.ac.ap.civilization.model.map.HexTile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class UnitMovementService {

    private final GameState gameState;

    public UnitMovementService(GameState gameState) {
        this.gameState = Objects.requireNonNull(
                gameState,
                "Game state cannot be null"
        );
    }

    public boolean canMoveUnit(
            int unitId,
            HexCoordinate destination
    ) {
        Objects.requireNonNull(
                destination,
                "Movement destination cannot be null"
        );

        Optional<Unit> possibleUnit =
                gameState.getUnitById(unitId);

        if (!possibleUnit.isPresent()) {
            return false;
        }

        Optional<HexTile> possibleDestination =
                gameState.getGameMap().getTile(destination);

        if (!possibleDestination.isPresent()) {
            return false;
        }

        Unit unit = possibleUnit.get();
        HexTile destinationTile = possibleDestination.get();

        return unit.getPosition().isAdjacentTo(destination) &&
                destinationTile.isNormallyPassable() &&
                unit.canSpendActionPoints(
                        destinationTile.getBaseMovementCost()
                );
    }

    public List<HexTile> getValidMovementDestinations(
            int unitId
    ) {
        Unit unit = getRequiredUnit(unitId);
        GameMap gameMap = gameState.getGameMap();

        List<HexTile> validDestinations =
                new ArrayList<>();

        for (HexTile neighbor : gameMap.getValidNeighbors(
                unit.getPosition()
        )) {
            if (neighbor.isNormallyPassable() &&
                    unit.canSpendActionPoints(
                            neighbor.getBaseMovementCost()
                    )) {

                validDestinations.add(neighbor);
            }
        }

        return Collections.unmodifiableList(
                validDestinations
        );
    }

    public UnitMovementResult moveUnit(
            int unitId,
            HexCoordinate destination
    ) {
        Objects.requireNonNull(
                destination,
                "Movement destination cannot be null"
        );

        Unit unit = getRequiredUnit(unitId);
        HexTile destinationTile =
                getRequiredDestination(destination);

        validateAdjacency(unit, destination);
        validatePassableDestination(destinationTile);

        int movementCost =
                destinationTile.getBaseMovementCost();

        validateActionPoints(unit, movementCost);

        HexCoordinate start = unit.getPosition();

        unit.spendActionPoints(movementCost);
        unit.moveTo(destination);
        gameState.refreshVisibility();

        return new UnitMovementResult(
                unit.getId(),
                start,
                destination,
                movementCost,
                unit.getCurrentActionPoints()
        );
    }

    private Unit getRequiredUnit(int unitId) {
        return gameState
                .getUnitById(unitId)
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "No unit exists with id: " +
                                        unitId
                        )
                );
    }

    private HexTile getRequiredDestination(
            HexCoordinate destination
    ) {
        return gameState
                .getGameMap()
                .getTile(destination)
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Movement destination is outside the map"
                        )
                );
    }

    private void validateAdjacency(
            Unit unit,
            HexCoordinate destination
    ) {
        if (!unit.getPosition().isAdjacentTo(destination)) {
            throw new IllegalArgumentException(
                    "A unit can move only to an adjacent hex"
            );
        }
    }

    private void validatePassableDestination(
            HexTile destinationTile
    ) {
        if (!destinationTile.isNormallyPassable()) {
            throw new IllegalStateException(
                    "The destination terrain is not passable"
            );
        }
    }

    private void validateActionPoints(
            Unit unit,
            int movementCost
    ) {
        if (!unit.canSpendActionPoints(movementCost)) {
            throw new IllegalStateException(
                    "Unit does not have enough action points " +
                            "to enter the destination terrain"
            );
        }
    }
}