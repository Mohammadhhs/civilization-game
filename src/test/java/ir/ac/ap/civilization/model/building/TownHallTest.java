package ir.ac.ap.civilization.model.building;

import ir.ac.ap.civilization.model.resource.ResourceStockpile;
import ir.ac.ap.civilization.model.resource.ResourceType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TownHallTest {

    @Test
    void newTownHallShouldStartAtBaseCamp() {
        TownHall townHall = new TownHall();

        assertEquals(
                TownHallLevel.BASE_CAMP,
                townHall.getCurrentLevel()
        );

        assertEquals(200, townHall.getCurrentHp());
        assertEquals(200, townHall.getMaximumHp());

        assertEquals(
                100,
                townHall.getStockpile()
                        .getCapacityPerResource()
        );

        assertFalse(townHall.isBusy());
        assertEquals("Idle", townHall.getActiveCommandName());
        assertEquals(0, townHall.getRemainingProductionTurns());
    }

    @Test
    void townHallShouldProduceBaseFoodAndWood() {
        TownHall townHall = new TownHall();

        townHall.produceBaseResources();

        assertEquals(
                4,
                townHall.getStockpile()
                        .getAmount(ResourceType.FOOD)
        );

        assertEquals(
                4,
                townHall.getStockpile()
                        .getAmount(ResourceType.WOOD)
        );

        assertEquals(
                0,
                townHall.getStockpile()
                        .getAmount(ResourceType.STONE)
        );

        assertEquals(
                0,
                townHall.getStockpile()
                        .getAmount(ResourceType.IRON)
        );
    }

    @Test
    void startingUpgradeShouldSpendResourcesImmediately() {
        TownHall townHall = createTownHallWithResources(
                50,
                50,
                0
        );

        townHall.startUpgrade();

        assertTrue(townHall.isBusy());

        assertEquals(
                "Upgrade Town Hall to Settlement",
                townHall.getActiveCommandName()
        );

        assertEquals(
                3,
                townHall.getRemainingProductionTurns()
        );

        assertEquals(
                0,
                townHall.getStockpile()
                        .getAmount(ResourceType.WOOD)
        );

        assertEquals(
                0,
                townHall.getStockpile()
                        .getAmount(ResourceType.STONE)
        );
    }

    @Test
    void townHallShouldRejectSecondCommandWhileBusy() {
        TownHall townHall = createTownHallWithResources(
                100,
                100,
                0
        );

        townHall.startUpgrade();

        assertThrows(
                IllegalStateException.class,
                townHall::startUpgrade
        );

        assertEquals(
                3,
                townHall.getRemainingProductionTurns()
        );
    }

    @Test
    void upgradeShouldCompleteAfterRequiredTurns() {
        TownHall townHall = createTownHallWithResources(
                50,
                50,
                0
        );

        townHall.startUpgrade();

        townHall.advanceProductionQueue();
        assertEquals(2, townHall.getRemainingProductionTurns());

        townHall.advanceProductionQueue();
        assertEquals(1, townHall.getRemainingProductionTurns());

        townHall.advanceProductionQueue();

        assertEquals(
                TownHallLevel.SETTLEMENT,
                townHall.getCurrentLevel()
        );

        assertEquals(
                200,
                townHall.getStockpile()
                        .getCapacityPerResource()
        );

        assertFalse(townHall.isBusy());
    }

    @Test
    void settlementUpgradeShouldHealFiftyHp() {
        TownHall townHall = createTownHallWithResources(
                50,
                50,
                0
        );

        townHall.takeDamage(80);

        assertEquals(120, townHall.getCurrentHp());

        townHall.startUpgrade();
        advanceQueue(townHall, 3);

        assertEquals(170, townHall.getCurrentHp());
    }

    @Test
    void healingShouldNotExceedMaximumHp() {
        TownHall townHall = createTownHallWithResources(
                50,
                50,
                0
        );

        townHall.takeDamage(20);

        townHall.startUpgrade();
        advanceQueue(townHall, 3);

        assertEquals(
                townHall.getMaximumHp(),
                townHall.getCurrentHp()
        );
    }

    @Test
    void cancellingCommandShouldNotRefundResources() {
        TownHall townHall = createTownHallWithResources(
                80,
                70,
                0
        );

        townHall.startUpgrade();
        townHall.cancelCurrentCommand();

        assertFalse(townHall.isBusy());

        assertEquals(
                30,
                townHall.getStockpile()
                        .getAmount(ResourceType.WOOD)
        );

        assertEquals(
                20,
                townHall.getStockpile()
                        .getAmount(ResourceType.STONE)
        );
    }

    @Test
    void insufficientResourcesShouldNotStartUpgrade() {
        TownHall townHall = createTownHallWithResources(
                49,
                50,
                0
        );

        assertThrows(
                IllegalStateException.class,
                townHall::startUpgrade
        );

        assertFalse(townHall.isBusy());

        assertEquals(
                49,
                townHall.getStockpile()
                        .getAmount(ResourceType.WOOD)
        );

        assertEquals(
                50,
                townHall.getStockpile()
                        .getAmount(ResourceType.STONE)
        );
    }

    @Test
    void townHallShouldUpgradeFromSettlementToCapital() {
        TownHall townHall = createTownHallWithResources(
                50,
                50,
                0
        );

        townHall.startUpgrade();
        advanceQueue(townHall, 3);

        townHall.getStockpile().add(
                ResourceType.STONE,
                100
        );

        townHall.getStockpile().add(
                ResourceType.IRON,
                50
        );

        townHall.startUpgrade();

        assertEquals(
                5,
                townHall.getRemainingProductionTurns()
        );

        advanceQueue(townHall, 5);

        assertEquals(
                TownHallLevel.CAPITAL,
                townHall.getCurrentLevel()
        );

        assertEquals(
                300,
                townHall.getStockpile()
                        .getCapacityPerResource()
        );
    }

    @Test
    void capitalShouldRejectFurtherUpgrade() {
        TownHall townHall = createTownHallWithResources(
                50,
                50,
                0
        );

        townHall.startUpgrade();
        advanceQueue(townHall, 3);

        townHall.getStockpile().add(
                ResourceType.STONE,
                100
        );

        townHall.getStockpile().add(
                ResourceType.IRON,
                50
        );

        townHall.startUpgrade();
        advanceQueue(townHall, 5);

        assertThrows(
                IllegalStateException.class,
                townHall::startUpgrade
        );
    }

    @Test
    void cancellingWithoutActiveCommandShouldFail() {
        TownHall townHall = new TownHall();

        assertThrows(
                IllegalStateException.class,
                townHall::cancelCurrentCommand
        );
    }

    private TownHall createTownHallWithResources(
            int wood,
            int stone,
            int iron
    ) {
        TownHall townHall = new TownHall();
        ResourceStockpile stockpile =
                townHall.getStockpile();

        stockpile.add(ResourceType.WOOD, wood);
        stockpile.add(ResourceType.STONE, stone);
        stockpile.add(ResourceType.IRON, iron);

        return townHall;
    }

    private void advanceQueue(
            TownHall townHall,
            int turns
    ) {
        for (int turn = 0; turn < turns; turn++) {
            townHall.advanceProductionQueue();
        }
    }
}