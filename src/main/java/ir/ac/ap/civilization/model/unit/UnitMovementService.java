package ir.ac.ap.civilization.model.unit;

import ir.ac.ap.civilization.model.game.GameState;
import ir.ac.ap.civilization.model.map.GameMap;
import ir.ac.ap.civilization.model.map.HexCoordinate;
import ir.ac.ap.civilization.model.map.HexTile;
import ir.ac.ap.civilization.model.map.TerrainType;
import ir.ac.ap.civilization.model.technology.TechnologyType;

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

        return !isStationedWorker(unit) &&
                unit.getPosition().isAdjacentTo(destination) &&
                isPassableForUnit(destinationTile) &&
                unit.canSpendActionPoints(
                        calculateMovementCost(unit, destinationTile)
                );
    }

    public List<HexTile> getValidMovementDestinations(
            int unitId
    ) {
        Unit unit = getRequiredUnit(unitId);
        GameMap gameMap = gameState.getGameMap();

        List<HexTile> validDestinations =
                new ArrayList<>();

        if (isStationedWorker(unit)) {
            return Collections.emptyList();
        }

        for (HexTile neighbor : gameMap.getValidNeighbors(
                unit.getPosition()
        )) {
            if (isPassableForUnit(neighbor) &&
                    unit.canSpendActionPoints(
                            calculateMovementCost(unit, neighbor)
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

        validateWorkerIsNotStationed(unit);
        validateAdjacency(unit, destination);
        validatePassableDestination(destinationTile);

        int movementCost = calculateMovementCost(unit, destinationTile);

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
        if (!isPassableForUnit(destinationTile)) {
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

    private boolean isStationedWorker(Unit unit) {
        return unit instanceof Worker &&
                ((Worker) unit).isStationed();
    }

    private int calculateMovementCost(Unit unit, HexTile destinationTile) {
        if (destinationTile.getTerrainType() == TerrainType.SEA) {
            return unit.getCurrentActionPoints();
        }
        int cost = gameState.getPhaseTwoState()
                .getInfrastructure()
                .movementCost(
                        unit.getPosition(),
                        destinationTile.getCoordinate(),
                        destinationTile.getBaseMovementCost()
                );
        switch (gameState.getPhaseTwoState().getSeason()) {
            case WINTER:
                return cost + 1;
            case AUTUMN:
                return destinationTile.getTerrainType().name().equals("SEA")
                        ? cost + 1 : cost;
            default:
                return cost;
        }
    }

    private boolean isPassableForUnit(HexTile tile) {
        return tile.isNormallyPassable() ||
                tile.getTerrainType() == TerrainType.SEA &&
                        gameState.getTownHall().hasTechnology(
                                TechnologyType.NAVIGATION
                        );
    }

    private void validateWorkerIsNotStationed(Unit unit) {
        if (isStationedWorker(unit)) {
            throw new IllegalStateException(
                    "Stationed Worker must leave the building before moving"
            );
        }
    }
}
