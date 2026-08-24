package ir.ac.ap.civilization.controller;

import ir.ac.ap.civilization.model.building.BuildingType;
import ir.ac.ap.civilization.model.building.ResourceBuilding;
import ir.ac.ap.civilization.model.game.GameState;
import ir.ac.ap.civilization.model.game.ResourceBuildingService;
import ir.ac.ap.civilization.model.game.TurnManager;
import ir.ac.ap.civilization.model.map.HexCoordinate;
import ir.ac.ap.civilization.model.map.HexTile;
import ir.ac.ap.civilization.model.technology.TechnologyType;
import ir.ac.ap.civilization.model.unit.UnitMovementResult;
import ir.ac.ap.civilization.model.unit.UnitMovementService;

import java.util.List;
import java.util.Objects;

public final class GameController {

    private final GameState gameState;
    private final TurnManager turnManager;
    private final UnitMovementService unitMovementService;
    private final ResourceBuildingService
            resourceBuildingService;

    public GameController(GameState gameState) {
        this.gameState = Objects.requireNonNull(
                gameState,
                "Game state cannot be null"
        );

        this.turnManager = new TurnManager(gameState);

        this.unitMovementService =
                new UnitMovementService(gameState);

        this.resourceBuildingService =
                new ResourceBuildingService(gameState);
    }

    public GameState getGameState() {
        return gameState;
    }

    public void endTurn() {
        turnManager.endTurn();
    }

    public void startTownHallUpgrade() {
        gameState.getTownHall().startUpgrade();
    }

    public void cancelTownHallCommand() {
        gameState.getTownHall()
                .cancelCurrentCommand();
    }

    public void startTechnologyResearch(
            TechnologyType technologyType
    ) {
        gameState.getTownHall()
                .startResearch(technologyType);
    }

    public boolean canStartTechnologyResearch(
            TechnologyType technologyType
    ) {
        return gameState.getTownHall()
                .canStartResearch(technologyType);
    }

    public boolean canMoveUnit(
            int unitId,
            HexCoordinate destination
    ) {
        return unitMovementService.canMoveUnit(
                unitId,
                destination
        );
    }

    public boolean canConstructResourceBuilding(
            int builderId,
            BuildingType buildingType
    ) {
        return resourceBuildingService.canConstruct(
                builderId,
                buildingType
        );
    }

    public ResourceBuilding constructResourceBuilding(
            int builderId,
            BuildingType buildingType
    ) {
        return resourceBuildingService.construct(
                builderId,
                buildingType
        );
    }

    public boolean canStationWorker(int workerId) {
        return resourceBuildingService
                .canStationWorker(workerId);
    }

    public void stationWorker(int workerId) {
        resourceBuildingService.stationWorker(workerId);
    }

    public boolean canUnstationWorker(int workerId) {
        return resourceBuildingService
                .canUnstationWorker(workerId);
    }

    public void unstationWorker(int workerId) {
        resourceBuildingService.unstationWorker(workerId);
    }

    public List<HexTile> getValidMovementDestinations(
            int unitId
    ) {
        return unitMovementService
                .getValidMovementDestinations(unitId);
    }

    public UnitMovementResult moveUnit(
            int unitId,
            HexCoordinate destination
    ) {
        return unitMovementService.moveUnit(
                unitId,
                destination
        );
    }
}
