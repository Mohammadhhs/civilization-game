package ir.ac.ap.civilization.controller;

import ir.ac.ap.civilization.model.building.BuildingType;
import ir.ac.ap.civilization.model.building.ResourceBuilding;
import ir.ac.ap.civilization.model.building.Settlement;
import ir.ac.ap.civilization.model.game.GameState;
import ir.ac.ap.civilization.model.game.BorderExpansionService;
import ir.ac.ap.civilization.model.game.ResourceBuildingService;
import ir.ac.ap.civilization.model.game.SettlementConstructionService;
import ir.ac.ap.civilization.model.game.TurnManager;
import ir.ac.ap.civilization.model.game.UnitProductionService;
import ir.ac.ap.civilization.model.game.CombatResult;
import ir.ac.ap.civilization.model.game.CombatService;
import ir.ac.ap.civilization.model.game.SpecialBuildingService;
import ir.ac.ap.civilization.model.game.InfrastructureService;
import ir.ac.ap.civilization.model.infrastructure.Wall;
import ir.ac.ap.civilization.model.building.SpecialBuilding;
import ir.ac.ap.civilization.model.building.SpecialBuildingType;
import ir.ac.ap.civilization.model.trade.BazaarTradeLevel;
import ir.ac.ap.civilization.model.trade.TradeResult;
import ir.ac.ap.civilization.model.trade.TradeService;
import ir.ac.ap.civilization.model.tribe.TribeService;
import ir.ac.ap.civilization.model.resource.ResourceType;
import ir.ac.ap.civilization.model.map.HexCoordinate;
import ir.ac.ap.civilization.model.map.HexTile;
import ir.ac.ap.civilization.model.technology.TechnologyType;
import ir.ac.ap.civilization.model.unit.UnitMovementResult;
import ir.ac.ap.civilization.model.unit.UnitMovementService;
import ir.ac.ap.civilization.model.unit.UnitType;

import java.util.List;
import java.util.Objects;

public final class GameController {

    private final GameState gameState;
    private final TurnManager turnManager;
    private final UnitMovementService unitMovementService;
    private final ResourceBuildingService
            resourceBuildingService;
    private final SettlementConstructionService
            settlementConstructionService;
    private final UnitProductionService unitProductionService;
    private final BorderExpansionService borderExpansionService;
    private final CombatService combatService;
    private final TradeService tradeService;
    private final TribeService tribeService;
    private final SpecialBuildingService specialBuildingService;
    private final InfrastructureService infrastructureService;

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

        this.settlementConstructionService =
                new SettlementConstructionService(gameState);

        this.unitProductionService =
                new UnitProductionService(gameState);

        this.borderExpansionService =
                new BorderExpansionService(gameState);
        this.combatService = new CombatService(gameState);
        this.tradeService = new TradeService(gameState);
        this.tribeService = new TribeService(gameState);
        this.specialBuildingService = new SpecialBuildingService(gameState);
        this.infrastructureService = new InfrastructureService(gameState);
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

    public boolean canConstructSettlement(int builderId) {
        return settlementConstructionService
                .canConstruct(builderId);
    }

    public Settlement constructSettlement(int builderId) {
        return settlementConstructionService
                .construct(builderId);
    }

    public boolean canStartUnitProduction(UnitType unitType) {
        return unitProductionService.canStart(unitType);
    }

    public void startUnitProduction(UnitType unitType) {
        unitProductionService.start(unitType);
    }

    public boolean canExpandBorder(int unitId) {
        return borderExpansionService.canExpand(unitId);
    }

    public String getBorderExpansionProblem(int unitId) {
        return borderExpansionService
                .getExpansionProblem(unitId);
    }

    public int expandBorder(int unitId) {
        return borderExpansionService.expand(unitId);
    }

    public int getIdleUnitCount() {
        return gameState.getIdleUnits().size();
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

    public CombatResult attack(
            HexCoordinate attacker,
            HexCoordinate defender
    ) {
        return combatService.attack(attacker, defender);
    }

    public TradeResult tradeAtBazaar(
            ResourceType sold,
            ResourceType received,
            BazaarTradeLevel level
    ) {
        return tradeService.tradeAtBazaar(sold, received, level);
    }

    public TradeResult tradeAtPost(
            ResourceType sold,
            ResourceType received,
            int amount
    ) {
        return tradeService.tradeAtPost(sold, received, amount);
    }

    public TribeService getTribeService() {
        return tribeService;
    }

    public SpecialBuilding constructSpecialBuilding(
            int builderId, SpecialBuildingType type
    ) {
        return specialBuildingService.construct(builderId, type);
    }

    public void buildRoad(int builderId) {
        infrastructureService.buildRoad(builderId);
    }

    public Wall buildWall(int builderId, HexCoordinate otherSide) {
        return infrastructureService.buildWall(builderId, otherSide);
    }

    public void demolishBuilding(int builderId, int buildingId) {
        infrastructureService.demolishBuilding(builderId, buildingId);
    }
}
