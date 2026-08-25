package ir.ac.ap.civilization.model.game;

import ir.ac.ap.civilization.model.building.Settlement;
import ir.ac.ap.civilization.model.map.HexCoordinate;
import ir.ac.ap.civilization.model.map.HexTile;
import ir.ac.ap.civilization.model.resource.ResourceStockpile;
import ir.ac.ap.civilization.model.technology.TechnologyType;
import ir.ac.ap.civilization.model.unit.Builder;
import ir.ac.ap.civilization.model.unit.Unit;

import java.util.Objects;

public final class SettlementConstructionService {

    private final GameState gameState;

    public SettlementConstructionService(GameState gameState) {
        this.gameState = Objects.requireNonNull(
                gameState,
                "Game state cannot be null"
        );
    }

    public boolean canConstruct(int builderId) {
        try {
            validateConstruction(builderId);
            return true;
        } catch (
                IllegalArgumentException |
                IllegalStateException exception
        ) {
            return false;
        }
    }

    public Settlement construct(int builderId) {
        ConstructionContext context =
                validateConstruction(builderId);

        ResourceStockpile stockpile =
                gameState.getTownHall().getStockpile();

        stockpile.spend(Settlement.getConstructionCost());

        context.builder.spendActionPoints(
                Settlement.CONSTRUCTION_ACTION_POINT_COST
        );
        context.builder.useCharge();

        Settlement settlement = gameState.addSettlement(
                context.tile.getCoordinate()
        );

        if (!context.builder.hasCharges()) {
            gameState.removeUnit(context.builder);
        }

        gameState.refreshVisibility();
        return settlement;
    }

    private ConstructionContext validateConstruction(
            int builderId
    ) {
        Unit unit = gameState.getUnitById(builderId)
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Unit does not exist"
                        )
                );

        if (!(unit instanceof Builder)) {
            throw new IllegalArgumentException(
                    "Only a Builder can construct a settlement"
            );
        }

        Builder builder = (Builder) unit;
        HexCoordinate position = builder.getPosition();

        HexTile tile = gameState.getGameMap()
                .getTile(position)
                .orElseThrow(
                        () -> new IllegalStateException(
                                "Builder is outside the game map"
                        )
                );

        if (!tile.isInsidePlayerTerritory()) {
            throw new IllegalStateException(
                    "Settlement must be inside player territory"
            );
        }

        if (!tile.isDiscovered()) {
            throw new IllegalStateException(
                    "Settlement hex must be discovered"
            );
        }

        if (!tile.isConstructionAllowed()) {
            throw new IllegalStateException(
                    "Construction is not allowed on this terrain"
            );
        }

        if (!tile.getNaturalResources().isEmpty()) {
            throw new IllegalStateException(
                    "Settlement requires a hex without natural resources"
            );
        }

        if (position.equals(gameState.getTownHallCoordinate())) {
            throw new IllegalStateException(
                    "Cannot construct over the Town Hall"
            );
        }

        if (gameState.getResourceBuildingAt(position).isPresent() ||
                gameState.getSettlementAt(position).isPresent()) {
            throw new IllegalStateException(
                    "This hex already contains a building"
            );
        }

        if (!gameState.getTownHall().hasTechnology(
                TechnologyType.SETTLEMENT_CONSTRUCTION
        )) {
            throw new IllegalStateException(
                    "Settlement Construction technology is required"
            );
        }

        if (gameState.getEconomyStatus().isStarving()) {
            throw new IllegalStateException(
                    "City growth is stopped during starvation"
            );
        }

        if (!builder.hasCharges()) {
            throw new IllegalStateException(
                    "Builder has no remaining charges"
            );
        }

        if (!builder.canSpendActionPoints(
                Settlement.CONSTRUCTION_ACTION_POINT_COST
        )) {
            throw new IllegalStateException(
                    "Builder does not have enough action points"
            );
        }

        if (!gameState.getTownHall()
                .getStockpile()
                .canAfford(Settlement.getConstructionCost())) {
            throw new IllegalStateException(
                    "Not enough resources to construct a settlement"
            );
        }

        return new ConstructionContext(builder, tile);
    }

    private static final class ConstructionContext {

        private final Builder builder;
        private final HexTile tile;

        private ConstructionContext(
                Builder builder,
                HexTile tile
        ) {
            this.builder = builder;
            this.tile = tile;
        }
    }
}
