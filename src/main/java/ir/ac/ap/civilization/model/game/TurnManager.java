package ir.ac.ap.civilization.model.game;

import ir.ac.ap.civilization.model.building.TownHall;
import ir.ac.ap.civilization.model.resource.ResourceStockpile;
import ir.ac.ap.civilization.model.production.ProductionCommand;
import ir.ac.ap.civilization.model.building.SpecialBuilding;
import ir.ac.ap.civilization.model.building.SpecialBuildingType;
import ir.ac.ap.civilization.model.tribe.TribeService;

import java.util.Objects;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class TurnManager {

    private final GameState gameState;
    private final ResourceBuildingService
            resourceBuildingService;
    private final EconomyService economyService;
    private final UnitProductionService unitProductionService;
    private final List<TurnPhase> lastCompletedPhases;
    private final TribeService tribeService;
    private final DisasterService disasterService;

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

        this.lastCompletedPhases = new ArrayList<>();
        this.tribeService = new TribeService(gameState);
        this.disasterService = new DisasterService(gameState);
    }

    public List<TurnPhase> getLastCompletedPhases() {
        return Collections.unmodifiableList(
                new ArrayList<>(lastCompletedPhases)
        );
    }

    public void endTurn() {
        lastCompletedPhases.clear();
        gameState.beginOperation(GameOperation.PROCESSING_TURN);

        try {
            TownHall townHall =
                    gameState.getTownHall();

            ResourceStockpile stockpile =
                    townHall.getStockpile();

            gameState.restoreAllUnitActionPoints();
            complete(TurnPhase.RESTORE_ACTION_POINTS);

            townHall.produceBaseResources();
            complete(TurnPhase.TOWN_HALL_PRODUCTION);

            resourceBuildingService.produceAllResources();
            complete(TurnPhase.BUILDING_PRODUCTION);

            ProductionCommand completedCommand =
                    townHall.advanceProductionQueue();
            complete(TurnPhase.PRODUCTION_QUEUE);

            unitProductionService.deployCompletedCommand(
                    completedCommand
            );
            complete(TurnPhase.UNIT_DEPLOYMENT);

            economyService.applyEndTurnCosts();
            complete(TurnPhase.ECONOMY);

            tribeService.advanceTurn();
            complete(TurnPhase.TRIBE_BEHAVIOR);

            updateHappiness();
            complete(TurnPhase.HAPPINESS);

            gameState.getPhaseTwoState().advanceSeason();
            complete(TurnPhase.SEASON);

            disasterService.rollEndTurnDisaster();
            complete(TurnPhase.DISASTER);

            stockpile.applyCapacityLimit();
            complete(TurnPhase.STORAGE_LIMIT);

            gameState.incrementTurnNumber();
            complete(TurnPhase.ADVANCE_TURN);

            gameState.completeOperation();
        } catch (RuntimeException exception) {
            gameState.failOperation();
            throw exception;
        } catch (Error error) {
            gameState.failOperation();
            throw error;
        }
    }

    private void complete(TurnPhase phase) {
        lastCompletedPhases.add(phase);
    }

    private void updateHappiness() {
        int monumentBonus = 0;
        for (SpecialBuilding building : gameState.getSpecialBuildings()) {
            if (building.getType() == SpecialBuildingType.MONUMENT &&
                    !building.isDestroyed()) {
                monumentBonus += 2;
            }
        }
        if (monumentBonus > 0) {
            gameState.getPhaseTwoState().getHappiness().change(monumentBonus);
        }
        if (gameState.getActiveMilitaryUnitCount() >=
                gameState.getMilitaryUnitCap()) {
            gameState.getPhaseTwoState().getHappiness().change(-1);
        }
    }
}
