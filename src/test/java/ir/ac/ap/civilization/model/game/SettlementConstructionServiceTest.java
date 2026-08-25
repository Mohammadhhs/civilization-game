package ir.ac.ap.civilization.model.game;

import ir.ac.ap.civilization.model.building.Settlement;
import ir.ac.ap.civilization.model.map.GameMap;
import ir.ac.ap.civilization.model.map.HexCoordinate;
import ir.ac.ap.civilization.model.map.HexTile;
import ir.ac.ap.civilization.model.map.NaturalResourceType;
import ir.ac.ap.civilization.model.map.TerrainType;
import ir.ac.ap.civilization.model.resource.ResourceStockpile;
import ir.ac.ap.civilization.model.resource.ResourceType;
import ir.ac.ap.civilization.model.technology.TechnologyType;
import ir.ac.ap.civilization.model.unit.Builder;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SettlementConstructionServiceTest {

    private static final HexCoordinate CENTER =
            new HexCoordinate(0, 0);

    private static final HexCoordinate SETTLEMENT_POSITION =
            new HexCoordinate(1, 0);

    @Test
    void initialGameShouldHaveEightUnitCapacity() {
        GameState gameState = createGameState(false);

        assertEquals(5, gameState.getActiveUnitCount());
        assertEquals(8, gameState.getUnitCap());
        assertEquals(3, gameState.getAvailableUnitSlots());
        assertTrue(gameState.canCreateUnit());
        assertFalse(gameState.isOverUnitCap());
    }

    @Test
    void researchedSettlementShouldBeConstructedInstantly() {
        GameState gameState = createPreparedGameState(false);
        SettlementConstructionService service =
                new SettlementConstructionService(gameState);

        Builder builder = getBuilder(gameState);
        Settlement settlement = service.construct(builder.getId());

        assertEquals(SETTLEMENT_POSITION, settlement.getPosition());
        assertEquals(1, gameState.getSettlements().size());
        assertEquals(13, gameState.getUnitCap());
        assertEquals(8, gameState.getAvailableUnitSlots());
        assertEquals(2, builder.getCurrentActionPoints());
        assertEquals(2, builder.getRemainingCharges());
        assertConstructionResourcesAreZero(gameState);
    }

    @Test
    void missingTechnologyShouldFailWithoutSpendingAnything() {
        GameState gameState = createGameState(false);
        ResourceStockpile stockpile =
                gameState.getTownHall().getStockpile();

        stockpile.add(ResourceType.IRON, 20);

        SettlementConstructionService service =
                new SettlementConstructionService(gameState);

        Builder builder = getBuilder(gameState);

        assertFalse(service.canConstruct(builder.getId()));
        assertThrows(
                IllegalStateException.class,
                () -> service.construct(builder.getId())
        );

        assertEquals(80, stockpile.getAmount(ResourceType.WOOD));
        assertEquals(60, stockpile.getAmount(ResourceType.STONE));
        assertEquals(20, stockpile.getAmount(ResourceType.IRON));
        assertEquals(5, builder.getCurrentActionPoints());
        assertEquals(3, builder.getRemainingCharges());
        assertTrue(gameState.getSettlements().isEmpty());
    }

    @Test
    void tileWithNaturalResourceShouldRejectSettlement() {
        GameState gameState = createPreparedGameState(true);
        SettlementConstructionService service =
                new SettlementConstructionService(gameState);

        assertFalse(service.canConstruct(1));
        assertThrows(
                IllegalStateException.class,
                () -> service.construct(1)
        );
        assertTrue(gameState.getSettlements().isEmpty());
        assertEquals(5, getBuilder(gameState).getCurrentActionPoints());
        assertEquals(3, getBuilder(gameState).getRemainingCharges());
    }

    @Test
    void starvationShouldStopSettlementConstruction() {
        GameState gameState = createPreparedGameState(false);

        gameState.getTownHall().getStockpile().spend(
                ResourceType.FOOD,
                55
        );
        new EconomyService(gameState).applyEndTurnCosts();

        SettlementConstructionService service =
                new SettlementConstructionService(gameState);

        assertTrue(gameState.getEconomyStatus().isStarving());
        assertFalse(service.canConstruct(1));
        assertThrows(
                IllegalStateException.class,
                () -> service.construct(1)
        );
        assertConstructionResourcesAreAvailable(gameState);
    }

    @Test
    void lastBuilderChargeShouldConsumeBuilder() {
        GameState gameState = createPreparedGameState(false);
        Builder builder = getBuilder(gameState);

        builder.useCharge();
        builder.useCharge();

        new SettlementConstructionService(gameState)
                .construct(builder.getId());

        assertFalse(gameState.getUnitById(builder.getId()).isPresent());
        assertEquals(4, gameState.getActiveUnitCount());
        assertEquals(13, gameState.getUnitCap());
        assertEquals(9, gameState.getAvailableUnitSlots());
    }

    @Test
    void unpaidSettlementShouldBeDestroyedAndRemoveCapBonus() {
        GameState gameState = createPreparedGameState(false);
        Settlement settlement =
                new SettlementConstructionService(gameState)
                        .construct(1);

        EconomyService economyService =
                new EconomyService(gameState);

        economyService.applyEndTurnCosts();
        economyService.applyEndTurnCosts();

        assertEquals(2, settlement.getConsecutiveMissedUpkeepTurns());
        assertEquals(13, gameState.getUnitCap());

        economyService.applyEndTurnCosts();

        assertTrue(gameState.getSettlements().isEmpty());
        assertEquals(8, gameState.getUnitCap());
        assertEquals(
                1,
                gameState.getEconomyStatus()
                        .getBuildingsDestroyedLastTurn()
        );
    }

    private GameState createPreparedGameState(
            boolean addNaturalResource
    ) {
        GameState gameState = createGameState(addNaturalResource);

        gameState.getTownHall().getStockpile().add(
                ResourceType.IRON,
                20
        );

        gameState.getTownHall().startResearch(
                TechnologyType.SETTLEMENT_CONSTRUCTION
        );

        for (int turn = 0;
             turn < TechnologyType.SETTLEMENT_CONSTRUCTION
                     .getResearchTurns();
             turn++) {
            gameState.getTownHall().advanceProductionQueue();
        }

        ResourceStockpile stockpile =
                gameState.getTownHall().getStockpile();

        stockpile.add(ResourceType.WOOD, 20);
        stockpile.add(ResourceType.STONE, 30);
        stockpile.add(ResourceType.IRON, 20);

        return gameState;
    }

    private GameState createGameState(
            boolean addNaturalResource
    ) {
        Map<HexCoordinate, HexTile> tiles =
                new LinkedHashMap<>();

        for (HexCoordinate coordinate :
                CENTER.getCoordinatesWithinRadius(2)) {
            if (addNaturalResource &&
                    coordinate.equals(SETTLEMENT_POSITION)) {
                tiles.put(
                        coordinate,
                        new HexTile(
                                coordinate,
                                TerrainType.PLAIN,
                                Collections.singleton(
                                        NaturalResourceType.STONE
                                )
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

        return new GameState(new GameMap(2, 45005L, tiles));
    }

    private Builder getBuilder(GameState gameState) {
        return (Builder) gameState.getUnitById(1)
                .orElseThrow(AssertionError::new);
    }

    private void assertConstructionResourcesAreZero(
            GameState gameState
    ) {
        ResourceStockpile stockpile =
                gameState.getTownHall().getStockpile();

        assertEquals(0, stockpile.getAmount(ResourceType.WOOD));
        assertEquals(0, stockpile.getAmount(ResourceType.STONE));
        assertEquals(0, stockpile.getAmount(ResourceType.IRON));
    }

    private void assertConstructionResourcesAreAvailable(
            GameState gameState
    ) {
        ResourceStockpile stockpile =
                gameState.getTownHall().getStockpile();

        assertEquals(60, stockpile.getAmount(ResourceType.WOOD));
        assertEquals(50, stockpile.getAmount(ResourceType.STONE));
        assertEquals(20, stockpile.getAmount(ResourceType.IRON));
    }
}
