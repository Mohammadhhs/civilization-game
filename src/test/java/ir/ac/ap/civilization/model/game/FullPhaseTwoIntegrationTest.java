package ir.ac.ap.civilization.model.game;

import ir.ac.ap.civilization.model.disaster.DisasterType;
import ir.ac.ap.civilization.model.entity.Owner;
import ir.ac.ap.civilization.model.infrastructure.InfrastructureState;
import ir.ac.ap.civilization.model.map.HexCoordinate;
import ir.ac.ap.civilization.model.phase2.Season;
import ir.ac.ap.civilization.model.resource.ResourceType;
import ir.ac.ap.civilization.model.trade.BazaarTradeLevel;
import ir.ac.ap.civilization.model.trade.TradeService;
import ir.ac.ap.civilization.model.tribe.Tribe;
import ir.ac.ap.civilization.model.tribe.TribeRelationState;
import ir.ac.ap.civilization.model.tribe.TribeService;
import ir.ac.ap.civilization.model.unit.MilitaryUnit;
import ir.ac.ap.civilization.model.unit.UnitType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class FullPhaseTwoIntegrationTest {

    @Test
    void militaryFactoryAndCapsShouldSupportPhaseTwoUnits() {
        GameState state = new GameState(11L);
        UnitProductionService production = new UnitProductionService(state);

        assertTrue(production.canStart(UnitType.SWORDSMAN));
        assertFalse(production.canStart(UnitType.ARCHER));
        assertEquals(5, state.getMilitaryUnitCap());
    }

    @Test
    void combatShouldUseDiceSpendApAndDamageHostileUnits() {
        GameState state = new GameState(12L);
        HexCoordinate attackerHex = new HexCoordinate(1, 0);
        HexCoordinate defenderHex = new HexCoordinate(2, 0);
        MilitaryUnit attacker = new MilitaryUnit(
                100, UnitType.SWORDSMAN, attackerHex
        );
        MilitaryUnit defender = new MilitaryUnit(
                101, UnitType.SWORDSMAN, defenderHex,
                Owner.barbarian(1), 100
        );
        state.addUnit(attacker);
        state.addUnit(defender);

        CombatResult result = new CombatService(state).attack(
                attackerHex, defenderHex
        );

        assertEquals(1, result.getAttackDice().size());
        assertEquals(2, result.getDefenseDice().size());
        assertEquals(2, attacker.getCurrentActionPoints());
        assertTrue(attacker.getCurrentHitPoints() < 100 ||
                defender.getCurrentHitPoints() < 100);
        assertTrue(state.isStableForPersistence());
    }

    @Test
    void tradeShouldFloorRateAndAllowOnlyOneUsePerTurn() {
        GameState state = new GameState(13L);
        TradeService trade = new TradeService(state);

        assertEquals(30, trade.tradeAtBazaar(
                ResourceType.WOOD, ResourceType.IRON,
                BazaarTradeLevel.SMALL
        ).getReceivedAmount());
        assertThrows(IllegalStateException.class, () ->
                trade.tradeAtBazaar(ResourceType.WOOD, ResourceType.STONE,
                        BazaarTradeLevel.SMALL));
    }

    @Test
    void tribeRelationsMissionsAndWarShouldBeStateful() {
        GameState state = new GameState(14L);
        Tribe tribe = state.getTribes().get(0);
        TribeService service = new TribeService(state);

        service.sendGift(tribe.getId(), ResourceType.WOOD, 20);
        assertEquals(2, tribe.getRelation());
        service.declareWar(tribe.getId());
        assertEquals(TribeRelationState.ENEMY, tribe.getRelationState());
    }

    @Test
    void roadsRiversAndWallsShouldComposeMovementCost() {
        InfrastructureState state = new InfrastructureState();
        HexCoordinate first = new HexCoordinate(0, 0);
        HexCoordinate second = new HexCoordinate(1, 0);
        state.buildRoad(second);
        state.addRiver(first, second);
        state.buildWall(first, second);

        assertEquals(4, state.movementCost(first, second, 5));
    }

    @Test
    void seasonShouldAdvanceEveryFiveTurns() {
        GameState state = new GameState(15L);
        for (int index = 0; index < 5; index++) {
            state.getPhaseTwoState().advanceSeason();
        }
        assertEquals(Season.SUMMER, state.getPhaseTwoState().getSeason());
    }

    @Test
    void disastersShouldBeDeterministicAndLeaveTownHallAlive() {
        GameState state = new GameState(16L);
        new DisasterService(state).apply(
                DisasterType.EARTHQUAKE,
                state.getTownHallCoordinate()
        );
        assertTrue(state.getTownHall().getCurrentHp() >= 1);
        assertTrue(state.isStableForPersistence());
    }

    @Test
    void saveLoadShouldRestoreExactStateAndRandomSequence(@TempDir Path directory)
            throws Exception {
        GameState state = new GameState(17L);
        state.getPhaseTwoState().getHappiness().change(7);
        state.getPhaseTwoState().advanceSeason();
        long randomState = state.getRandomSource().getState();
        GameSaveService saves = new GameSaveService(directory);

        saves.save(state, SaveSlot.MANUAL_1, "integration");
        GameState loaded = saves.load(SaveSlot.MANUAL_1);

        assertEquals(state.getGameId(), loaded.getGameId());
        assertEquals(state.getTurnNumber(), loaded.getTurnNumber());
        assertEquals(state.getUnits().size(), loaded.getUnits().size());
        assertEquals(7, loaded.getPhaseTwoState().getHappiness().getValue());
        assertEquals(randomState, loaded.getRandomSource().getState());
        assertEquals(state.getRandomSource().nextInt(1000),
                loaded.getRandomSource().nextInt(1000));
        assertFalse(saves.isCorrupt(SaveSlot.MANUAL_1));
        assertEquals(1, saves.list().size());
    }
}
