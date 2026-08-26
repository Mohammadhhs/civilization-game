package ir.ac.ap.civilization.model.game;

import ir.ac.ap.civilization.model.building.SpecialBuilding;
import ir.ac.ap.civilization.model.building.SpecialBuildingType;
import ir.ac.ap.civilization.model.entity.Owner;
import ir.ac.ap.civilization.model.map.HexCoordinate;
import ir.ac.ap.civilization.model.map.HexTile;
import ir.ac.ap.civilization.model.map.TerrainType;
import ir.ac.ap.civilization.model.resource.ResourceType;
import ir.ac.ap.civilization.model.unit.Builder;
import ir.ac.ap.civilization.model.unit.Unit;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

public final class SpecialBuildingService {
    private final GameState gameState;
    public SpecialBuildingService(GameState gameState) { this.gameState = gameState; }

    public SpecialBuilding construct(int builderId, SpecialBuildingType type) {
        Objects.requireNonNull(type, "Building type cannot be null");
        Unit unit = gameState.getUnitById(builderId)
                .orElseThrow(() -> new IllegalArgumentException("Unknown builder"));
        if (!(unit instanceof Builder)) throw new IllegalArgumentException("Only Builder can construct");
        Builder builder = (Builder) unit;
        HexCoordinate position = builder.getPosition();
        HexTile tile = gameState.getGameMap().getTile(position)
                .orElseThrow(() -> new IllegalStateException("Builder is outside map"));
        if (!tile.isInsidePlayerTerritory() || !tile.isConstructionAllowed()) {
            throw new IllegalStateException("Construction requires buildable player territory");
        }
        if (gameState.getTownHall().getCurrentLevel().getLevelNumber() <
                type.getRequiredTownHallLevel()) {
            throw new IllegalStateException("Town Hall level is too low");
        }
        if (type == SpecialBuildingType.DOCK && !isCoastal(position)) {
            throw new IllegalStateException("Dock must be built on coastal land");
        }
        Map<ResourceType, Integer> cost = cost(type);
        gameState.getTownHall().getStockpile().spend(cost);
        builder.spendActionPoints(2);
        builder.useCharge();
        SpecialBuilding building = gameState.addSpecialBuilding(
                type, position, Owner.PLAYER
        );
        if (!builder.hasCharges()) gameState.removeUnit(builder);
        return building;
    }

    private boolean isCoastal(HexCoordinate coordinate) {
        for (HexTile neighbor : gameState.getGameMap().getValidNeighbors(coordinate)) {
            if (neighbor.getTerrainType() == TerrainType.SEA) return true;
        }
        return false;
    }

    private Map<ResourceType, Integer> cost(SpecialBuildingType type) {
        EnumMap<ResourceType, Integer> result = new EnumMap<>(ResourceType.class);
        switch (type) {
            case BAZAAR: result.put(ResourceType.WOOD, 50); result.put(ResourceType.STONE, 30); break;
            case DOCK: result.put(ResourceType.WOOD, 60); result.put(ResourceType.STONE, 20); break;
            case MONUMENT: result.put(ResourceType.WOOD, 40); result.put(ResourceType.STONE, 60); break;
            case TRADING_POST: throw new IllegalStateException("Trading Post is neutral and cannot be built");
            default: throw new IllegalStateException("Unknown building type");
        }
        return result;
    }
}
