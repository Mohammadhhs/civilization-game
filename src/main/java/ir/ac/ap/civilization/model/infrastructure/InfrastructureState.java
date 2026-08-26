package ir.ac.ap.civilization.model.infrastructure;

import ir.ac.ap.civilization.model.map.HexCoordinate;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.io.Serializable;

public final class InfrastructureState implements Serializable {
    private final Set<HexCoordinate> roads = new LinkedHashSet<>();
    private final Set<HexEdge> rivers = new LinkedHashSet<>();
    private final Map<HexEdge, Wall> walls = new LinkedHashMap<>();

    public Set<HexCoordinate> getRoads() {
        return Collections.unmodifiableSet(new LinkedHashSet<>(roads));
    }
    public Set<HexEdge> getRivers() {
        return Collections.unmodifiableSet(new LinkedHashSet<>(rivers));
    }
    public Map<HexEdge, Wall> getWalls() {
        return Collections.unmodifiableMap(new LinkedHashMap<>(walls));
    }
    public void buildRoad(HexCoordinate coordinate) { roads.add(coordinate); }
    public void removeRoad(HexCoordinate coordinate) { roads.remove(coordinate); }
    public void addRiver(HexCoordinate one, HexCoordinate two) {
        rivers.add(new HexEdge(one, two));
    }
    public Wall buildWall(HexCoordinate one, HexCoordinate two) {
        HexEdge edge = new HexEdge(one, two);
        Wall wall = new Wall(edge);
        walls.put(edge, wall);
        return wall;
    }
    public boolean hasRiver(HexCoordinate one, HexCoordinate two) {
        return rivers.contains(new HexEdge(one, two));
    }
    public Optional<Wall> getWall(HexCoordinate one, HexCoordinate two) {
        return Optional.ofNullable(walls.get(new HexEdge(one, two)));
    }
    public int movementCost(HexCoordinate from, HexCoordinate to, int baseCost) {
        int cost = roads.contains(to) ? 1 : baseCost;
        if (hasRiver(from, to)) cost++;
        if (getWall(from, to).isPresent()) cost += 2;
        return cost;
    }
}
