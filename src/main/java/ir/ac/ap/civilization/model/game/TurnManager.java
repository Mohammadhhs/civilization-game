package ir.ac.ap.civilization.model.game;

import ir.ac.ap.civilization.model.building.TownHall;
import ir.ac.ap.civilization.model.resource.ResourceStockpile;
import ir.ac.ap.civilization.model.production.ProductionCommand;

import java.util.Objects;

public final class TurnManager {

    private final GameState gameState;
    private final ResourceBuildingService
            resourceBuildingService;
    private final EconomyService economyService;
    private final UnitProductionService unitProductionService;

    public TurnManager(GameState gameState) {
        this.gameState = Objects.requireNonNull(
                gameState,
                "Game state cannot be null"
        );

        this.resourceBuildingService =
                new ResourceBuildingService(gameState);

        this.economyService =
                new EconomyService(gameState);

        this.unitProductionService =
                new UnitProductionService(gameState);
    }

    public void endTurn() {
        TownHall townHall =
                gameState.getTownHall();

        ResourceStockpile stockpile =
                townHall.getStockpile();

        gameState.restoreAllUnitActionPoints();

        townHall.produceBaseResources();

        resourceBuildingService.produceAllResources();

        ProductionCommand completedCommand =
                townHall.advanceProductionQueue();

        unitProductionService.deployCompletedCommand(
                completedCommand
        );

        economyService.applyEndTurnCosts();

        stockpile.applyCapacityLimit();

        gameState.incrementTurnNumber();
    }
}
