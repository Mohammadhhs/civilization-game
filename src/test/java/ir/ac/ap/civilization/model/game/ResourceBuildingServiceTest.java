package ir.ac.ap.civilization.model.game;

import ir.ac.ap.civilization.model.building.BuildingType;
import ir.ac.ap.civilization.model.building.ResourceBuilding;
import ir.ac.ap.civilization.model.map.GameMap;
import ir.ac.ap.civilization.model.map.HexCoordinate;
import ir.ac.ap.civilization.model.map.HexTile;
import ir.ac.ap.civilization.model.map.NaturalResourceType;
import ir.ac.ap.civilization.model.map.TerrainType;
import ir.ac.ap.civilization.model.resource.ResourceType;
import ir.ac.ap.civilization.model.technology.TechnologyType;
import ir.ac.ap.civilization.model.unit.Builder;
import ir.ac.ap.civilization.model.unit.UnitMovementService;
import ir.ac.ap.civilization.model.unit.Worker;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResourceBuildingServiceTest {

    private static final HexCoordinate CENTER =
            new HexCoordinate(0, 0);

    private static final HexCoordinate BUILDING_POSITION =
            new HexCoordinate(1, 0);

    @Test
    void builderShouldConstructBuildingInstantly() {
        GameState gameState = createGameState(
                TerrainType.FOREST,
                NaturalResourceType.WOOD,
                20
        );

        ResourceBuildingService service =
                new ResourceBuildingService(gameState);

        Builder builder = (Builder) gameState
                .getUnitById(1)
                .orElseThrow(AssertionError::new);

        ResourceBuilding building = service.construct(
                builder.getId(),
                BuildingType.LUMBER_MILL
        );

        assertEquals(
                BuildingType.LUMBER_MILL,
                building.getBuildingType()
        );
        assertEquals(BUILDING_POSITION, building.getPosition());
        assertEquals(4, builder.getCurrentActionPoints());
        assertEquals(2, builder.getRemainingCharges());
        assertEquals(
                65,
                gameState.getTownHall()
                        .getStockpile()
                        .getAmount(ResourceType.WOOD)
        );
    }

    @Test
    void failedConstructionShouldNotSpendAnything() {
        GameState gameState = createGameState(
                TerrainType.FOREST,
                NaturalResourceType.WOOD,
                20
        );

        ResourceBuildingService service =
                new ResourceBuildingService(gameState);

        Builder builder = (Builder) gameState
                .getUnitById(1)
                .orElseThrow(AssertionError::new);

        gameState.getTownHall().getStockpile().spend(
                ResourceType.WOOD,
                70
        );

        assertThrows(
                IllegalStateException.class,
                () -> service.construct(
                        builder.getId(),
                        BuildingType.LUMBER_MILL
                )
        );

        assertEquals(5, builder.getCurrentActionPoints());
        assertEquals(3, builder.getRemainingCharges());
        assertEquals(
                10,
                gameState.getTownHall()
                        .getStockpile()
                        .getAmount(ResourceType.WOOD)
        );
        assertTrue(gameState.getResourceBuildings().isEmpty());
    }

    @Test
    void buildingShouldRequireMatchingNaturalResource() {
        GameState gameState = createGameState(
                TerrainType.FOREST,
                NaturalResourceType.WOOD,
                20
        );

        ResourceBuildingService service =
                new ResourceBuildingService(gameState);

        assertFalse(
                service.canConstruct(1, BuildingType.FARM)
        );

        assertThrows(
                IllegalStateException.class,
                () -> service.construct(1, BuildingType.FARM)
        );
    }

    @Test
    void stoneMineShouldRequireStoneMiningTechnology() {
        GameState gameState = createGameState(
                TerrainType.MOUNTAIN,
                NaturalResourceType.STONE,
                20
        );

        ResourceBuildingService service =
                new ResourceBuildingService(gameState);

        assertFalse(
                service.canConstruct(
                        1,
                        BuildingType.STONE_MINE
                )
        );

        researchAndComplete(
                gameState,
                TechnologyType.STONE_MINING
        );

        assertTrue(
                service.canConstruct(
                        1,
                        BuildingType.STONE_MINE
                )
        );
    }

    @Test
    void stationedWorkerShouldProduceAndConsumeDeposit() {
        GameState gameState = createGameState(
                TerrainType.FOREST,
                NaturalResourceType.WOOD,
                20
        );

        ResourceBuildingService service =
                prepareLumberMillWithWorker(gameState);

        int produced = service.produceAllResources();

        assertEquals(4, produced);
        assertEquals(
                69,
                gameState.getTownHall()
                        .getStockpile()
                        .getAmount(ResourceType.WOOD)
        );
        assertEquals(
                16,
                getBuildingTile(gameState)
                        .getRemainingResourceAmount(
                                NaturalResourceType.WOOD
                        )
        );
    }

    @Test
    void endTurnShouldRunBuildingProduction() {
        GameState gameState = createGameState(
                TerrainType.FOREST,
                NaturalResourceType.WOOD,
                20
        );

        prepareLumberMillWithWorker(gameState);

        new TurnManager(gameState).endTurn();

        assertEquals(
                72,
                gameState.getTownHall()
                        .getStockpile()
                        .getAmount(ResourceType.WOOD)
        );
        assertEquals(
                16,
                getBuildingTile(gameState)
                        .getRemainingResourceAmount(
                                NaturalResourceType.WOOD
                        )
        );
    }

    @Test
    void buildingWithoutWorkerShouldProduceNothing() {
        GameState gameState = createGameState(
                TerrainType.FOREST,
                NaturalResourceType.WOOD,
                20
        );

        ResourceBuildingService service =
                new ResourceBuildingService(gameState);

        service.construct(1, BuildingType.LUMBER_MILL);

        assertEquals(0, service.produceAllResources());
        assertEquals(
                20,
                getBuildingTile(gameState)
                        .getRemainingResourceAmount(
                                NaturalResourceType.WOOD
                        )
        );
    }

    @Test
    void depletedDepositShouldReleaseWorker() {
        GameState gameState = createGameState(
                TerrainType.FOREST,
                NaturalResourceType.WOOD,
                3
        );

        ResourceBuildingService service =
                prepareLumberMillWithWorker(gameState);

        Worker worker = (Worker) gameState
                .getUnitById(3)
                .orElseThrow(AssertionError::new);

        assertEquals(3, service.produceAllResources());
        assertFalse(worker.isStationed());
        assertEquals(
                0,
                gameState.getResourceBuildings()
                        .get(0)
                        .getStationedWorkerCount()
        );
        assertTrue(
                getBuildingTile(gameState)
                        .isNaturalResourceDepleted(
                                NaturalResourceType.WOOD
                        )
        );
    }

    @Test
    void stationedWorkerShouldNotMoveUntilLeavingBuilding() {
        GameState gameState = createGameState(
                TerrainType.FOREST,
                NaturalResourceType.WOOD,
                20
        );

        ResourceBuildingService service =
                prepareLumberMillWithWorker(gameState);

        UnitMovementService movementService =
                new UnitMovementService(gameState);

        assertTrue(
                movementService
                        .getValidMovementDestinations(3)
                        .isEmpty()
        );

        assertThrows(
                IllegalStateException.class,
                () -> movementService.moveUnit(
                        3,
                        new HexCoordinate(0, 0)
                )
        );

        service.unstationWorker(3);

        assertFalse(
                movementService
                        .getValidMovementDestinations(3)
                        .isEmpty()
        );
    }

    @Test
    void professionalToolsShouldIncreaseStoneProduction() {
        GameState gameState = createGameState(
                TerrainType.MOUNTAIN,
                NaturalResourceType.STONE,
                20
        );

        gameState.getTownHall().getStockpile().add(
                ResourceType.WOOD,
                45
        );
        gameState.getTownHall().getStockpile().add(
                ResourceType.IRON,
                20
        );

        researchAndComplete(
                gameState,
                TechnologyType.STONE_MINING
        );
        researchAndComplete(
                gameState,
                TechnologyType.IRON_MINING
        );
        researchAndComplete(
                gameState,
                TechnologyType.PROFESSIONAL_TOOLS
        );

        ResourceBuildingService service =
                new ResourceBuildingService(gameState);

        service.construct(1, BuildingType.STONE_MINE);
        moveWorkerToBuilding(gameState, 3);
        service.stationWorker(3);

        assertEquals(6, service.produceAllResources());
        assertEquals(
                14,
                getBuildingTile(gameState)
                        .getRemainingResourceAmount(
                                NaturalResourceType.STONE
                        )
        );
    }

    @Test
    void lastBuilderChargeShouldConsumeBuilder() {
        GameState gameState = createGameState(
                TerrainType.FOREST,
                NaturalResourceType.WOOD,
                20
        );

        ResourceBuildingService service =
                new ResourceBuildingService(gameState);

        Builder builder = (Builder) gameState
                .getUnitById(1)
                .orElseThrow(AssertionError::new);

        builder.useCharge();
        builder.useCharge();

        service.construct(1, BuildingType.LUMBER_MILL);

        assertFalse(gameState.getUnitById(1).isPresent());
        assertEquals(4, gameState.getUnits().size());
    }

    private ResourceBuildingService
    prepareLumberMillWithWorker(GameState gameState) {
        ResourceBuildingService service =
                new ResourceBuildingService(gameState);

        service.construct(1, BuildingType.LUMBER_MILL);
        moveWorkerToBuilding(gameState, 3);
        service.stationWorker(3);

        return service;
    }

    private void moveWorkerToBuilding(
            GameState gameState,
            int workerId
    ) {
        UnitMovementService movementService =
                new UnitMovementService(gameState);

        movementService.moveUnit(
                workerId,
                new HexCoordinate(1, -1)
        );

        movementService.moveUnit(
                workerId,
                BUILDING_POSITION
        );

        gameState.getUnitById(workerId)
                .orElseThrow(AssertionError::new)
                .restoreActionPoints();
    }

    private void researchAndComplete(
            GameState gameState,
            TechnologyType technologyType
    ) {
        gameState.getTownHall().startResearch(technologyType);

        for (int turn = 0;
             turn < technologyType.getResearchTurns();
             turn++) {
            gameState.getTownHall()
                    .advanceProductionQueue();
        }
    }

    private HexTile getBuildingTile(GameState gameState) {
        return gameState.getGameMap()
                .getTile(BUILDING_POSITION)
                .orElseThrow(AssertionError::new);
    }

    private GameState createGameState(
            TerrainType terrainType,
            NaturalResourceType naturalResourceType,
            int resourceAmount
    ) {
        Map<HexCoordinate, HexTile> tiles =
                new LinkedHashMap<>();

        for (HexCoordinate coordinate :
                CENTER.getCoordinatesWithinRadius(2)) {
            if (coordinate.equals(BUILDING_POSITION)) {
                Map<NaturalResourceType, Integer>
                        resourceAmounts =
                        Collections.singletonMap(
                                naturalResourceType,
                                resourceAmount
                        );

                tiles.put(
                        coordinate,
                        HexTile.withResourceAmounts(
                                coordinate,
                                terrainType,
                                resourceAmounts
                        )
                );
            } else {
                tiles.put(
                        coordinate,
                        new HexTile(
                                coordinate,
                                TerrainType.PLAIN
                        )
                );
            }
        }

        return new GameState(
                new GameMap(2, 7003L, tiles)
        );
    }
}
