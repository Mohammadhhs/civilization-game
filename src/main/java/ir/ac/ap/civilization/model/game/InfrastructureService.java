package ir.ac.ap.civilization.model.game;

import ir.ac.ap.civilization.model.building.MaintainedBuilding;
import ir.ac.ap.civilization.model.infrastructure.InfrastructureState;
import ir.ac.ap.civilization.model.infrastructure.Wall;
import ir.ac.ap.civilization.model.map.HexCoordinate;
import ir.ac.ap.civilization.model.map.HexTile;
import ir.ac.ap.civilization.model.map.TerrainType;
import ir.ac.ap.civilization.model.resource.ResourceStockpile;
import ir.ac.ap.civilization.model.resource.ResourceType;
import ir.ac.ap.civilization.model.unit.Builder;
import ir.ac.ap.civilization.model.unit.Unit;

import java.util.Objects;
import java.util.EnumMap;

public final class InfrastructureService {
    public static final int ACTION_POINT_COST = 1;
    private final GameState gameState;

    public InfrastructureService(GameState gameState) {
        this.gameState = Objects.requireNonNull(gameState, "Game state cannot be null");
    }

    public void buildRoad(int builderId) {
        Builder builder = requireBuilder(builderId);
        HexTile tile = requireTile(builder.getPosition());
        if (!tile.isInsidePlayerTerritory() || !tile.isConstructionAllowed()) {
            throw new IllegalStateException("Road requires buildable player territory");
        }
        ResourceStockpile stockpile = gameState.getTownHall().getStockpile();
        stockpile.spend(ResourceType.WOOD, 10);
        builder.spendActionPoints(ACTION_POINT_COST);
        gameState.getPhaseTwoState().getInfrastructure()
                .buildRoad(builder.getPosition());
    }

    public Wall buildWall(int builderId, HexCoordinate otherSide) {
        Builder builder = requireBuilder(builderId);
        HexCoordinate first = builder.getPosition();
        HexTile firstTile = requireTile(first);
        HexTile secondTile = requireTile(otherSide);
        if (!first.isAdjacentTo(otherSide)) {
            throw new IllegalArgumentException("Wall edge must touch Builder hex");
        }
        if (!firstTile.isDiscovered() || !secondTile.isDiscovered()) {
            throw new IllegalStateException("Both wall hexes must be discovered");
        }
        if (!firstTile.isInsidePlayerTerritory() && !secondTile.isInsidePlayerTerritory()) {
            throw new IllegalStateException("Wall must touch player territory");
        }
        if (forbiddenEdge(firstTile) || forbiddenEdge(secondTile)) {
            throw new IllegalStateException("Wall cannot border sea or mountain range");
        }
        ResourceStockpile stockpile = gameState.getTownHall().getStockpile();
        EnumMap<ResourceType, Integer> cost = new EnumMap<>(ResourceType.class);
        cost.put(ResourceType.WOOD, 5);
        cost.put(ResourceType.STONE, 15);
        stockpile.spend(cost);
        builder.spendActionPoints(ACTION_POINT_COST);
        return gameState.getPhaseTwoState().getInfrastructure()
                .buildWall(first, otherSide);
    }

    public void demolishBuilding(int builderId, int buildingId) {
        Builder builder = requireBuilder(builderId);
        MaintainedBuilding building = gameState.getBuildingById(buildingId)
                .orElseThrow(() -> new IllegalArgumentException("Unknown building"));
        if (!building.getOwner().isPlayer()) {
            throw new IllegalStateException("Only player buildings can be demolished");
        }
        if (!builder.getPosition().equals(building.getPosition()) &&
                !builder.getPosition().isAdjacentTo(building.getPosition())) {
            throw new IllegalStateException("Builder must be on or beside the building");
        }
        builder.spendActionPoints(ACTION_POINT_COST);
        gameState.removeMaintainedBuilding(building);
    }

    private Builder requireBuilder(int id) {
        Unit unit = gameState.getUnitById(id)
                .orElseThrow(() -> new IllegalArgumentException("Unknown Builder"));
        if (!(unit instanceof Builder)) throw new IllegalArgumentException("Only Builder can do this");
        Builder builder = (Builder) unit;
        if (!builder.canSpendActionPoints(ACTION_POINT_COST)) {
            throw new IllegalStateException("Builder has insufficient AP");
        }
        return builder;
    }

    private HexTile requireTile(HexCoordinate coordinate) {
        return gameState.getGameMap().getTile(coordinate)
                .orElseThrow(() -> new IllegalArgumentException("Hex is outside map"));
    }

    private boolean forbiddenEdge(HexTile tile) {
        return tile.getTerrainType() == TerrainType.SEA ||
                tile.getTerrainType() == TerrainType.MOUNTAIN_RANGE;
    }
}
