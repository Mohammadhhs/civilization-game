package ir.ac.ap.civilization.model.unit;

import ir.ac.ap.civilization.model.game.GameState;
import ir.ac.ap.civilization.model.map.HexCoordinate;
import ir.ac.ap.civilization.model.map.HexTile;
import ir.ac.ap.civilization.model.map.TerrainType;
import ir.ac.ap.civilization.model.technology.TechnologyType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.PriorityQueue;

/** A* route finder used by AI; returns steps without the starting hex. */
public final class UnitPathfinder {

    private final GameState gameState;

    public UnitPathfinder(GameState gameState) {
        this.gameState = Objects.requireNonNull(gameState, "Game state cannot be null");
    }

    public List<HexCoordinate> findPath(Unit unit, HexCoordinate destination) {
        Objects.requireNonNull(unit, "Unit cannot be null");
        Objects.requireNonNull(destination, "Destination cannot be null");
        HexCoordinate start = unit.getPosition();
        if (start.equals(destination)) return Collections.emptyList();
        if (!gameState.getGameMap().containsCoordinate(destination)) {
            return Collections.emptyList();
        }

        PriorityQueue<RouteNode> open = new PriorityQueue<>(
                Comparator.comparingInt(RouteNode::getEstimatedTotal));
        Map<HexCoordinate, Integer> costs = new HashMap<>();
        Map<HexCoordinate, HexCoordinate> previous = new HashMap<>();
        costs.put(start, 0);
        open.add(new RouteNode(start, start.distanceTo(destination)));

        while (!open.isEmpty()) {
            HexCoordinate current = open.remove().coordinate;
            if (current.equals(destination)) return reconstruct(previous, start, destination);
            int currentCost = costs.get(current);
            for (HexTile neighbor : gameState.getGameMap().getValidNeighbors(current)) {
                if (!isPassable(unit, neighbor)) continue;
                int candidate = currentCost + routeCost(current, neighbor);
                Integer known = costs.get(neighbor.getCoordinate());
                if (known != null && candidate >= known) continue;
                costs.put(neighbor.getCoordinate(), candidate);
                previous.put(neighbor.getCoordinate(), current);
                open.add(new RouteNode(neighbor.getCoordinate(), candidate +
                        neighbor.getCoordinate().distanceTo(destination)));
            }
        }
        return Collections.emptyList();
    }

    private boolean isPassable(Unit unit, HexTile tile) {
        if (unit.getUnitType().isNaval()) {
            return tile.getTerrainType() == TerrainType.SEA;
        }
        return tile.isNormallyPassable() ||
                unit.getOwner().isPlayer() &&
                tile.getTerrainType() == TerrainType.SEA &&
                gameState.getTownHall().hasTechnology(TechnologyType.NAVIGATION);
    }

    private int routeCost(HexCoordinate from, HexTile destination) {
        int cost = gameState.getPhaseTwoState().getInfrastructure().movementCost(
                from, destination.getCoordinate(), destination.getBaseMovementCost());
        switch (gameState.getPhaseTwoState().getSeason()) {
            case WINTER: return cost + 1;
            case AUTUMN:
                return destination.getTerrainType() == TerrainType.SEA ? cost + 1 : cost;
            default: return cost;
        }
    }

    private List<HexCoordinate> reconstruct(
            Map<HexCoordinate, HexCoordinate> previous,
            HexCoordinate start,
            HexCoordinate destination
    ) {
        List<HexCoordinate> reversed = new ArrayList<>();
        HexCoordinate cursor = destination;
        while (!cursor.equals(start)) {
            reversed.add(cursor);
            cursor = previous.get(cursor);
            if (cursor == null) return Collections.emptyList();
        }
        Collections.reverse(reversed);
        return Collections.unmodifiableList(reversed);
    }

    private static final class RouteNode {
        private final HexCoordinate coordinate;
        private final int estimatedTotal;
        private RouteNode(HexCoordinate coordinate, int estimatedTotal) {
            this.coordinate = coordinate;
            this.estimatedTotal = estimatedTotal;
        }
        private int getEstimatedTotal() { return estimatedTotal; }
    }
}
