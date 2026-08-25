package ir.ac.ap.civilization.model.building;

import ir.ac.ap.civilization.model.resource.ResourceStockpile;
import ir.ac.ap.civilization.model.resource.ResourceType;
import ir.ac.ap.civilization.model.technology.TechnologyType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TownHallTechnologyTest {

    @Test
    void startingResearchShouldSpendItsWholeCostImmediately() {
        TownHall townHall = createTownHallWithResources(
                30,
                10,
                0
        );

        townHall.startResearch(
                TechnologyType.STONE_MINING
        );

        assertTrue(townHall.isBusy());
        assertEquals(
                "Research Stone Mining",
                townHall.getActiveCommandName()
        );
        assertEquals(2, townHall.getRemainingProductionTurns());
        assertEquals(
                0,
                townHall.getStockpile()
                        .getAmount(ResourceType.WOOD)
        );
        assertEquals(
                10,
                townHall.getStockpile()
                        .getAmount(ResourceType.STONE)
        );
    }

    @Test
    void insufficientResearchCostShouldBeAtomic() {
        TownHall townHall = createTownHallWithResources(
                30,
                19,
                0
        );

        assertThrows(
                IllegalStateException.class,
                () -> townHall.startResearch(
                        TechnologyType.STORAGE_UPGRADE_I
                )
        );

        assertFalse(townHall.isBusy());
        assertEquals(
                30,
                townHall.getStockpile()
                        .getAmount(ResourceType.WOOD)
        );
        assertEquals(
                19,
                townHall.getStockpile()
                        .getAmount(ResourceType.STONE)
        );
    }

    @Test
    void researchShouldCompleteAfterRequiredTurns() {
        TownHall townHall = createTownHallWithResources(
                30,
                0,
                0
        );

        townHall.startResearch(
                TechnologyType.STONE_MINING
        );

        townHall.advanceProductionQueue();

        assertFalse(
                townHall.hasTechnology(
                        TechnologyType.STONE_MINING
                )
        );

        townHall.advanceProductionQueue();

        assertTrue(
                townHall.hasTechnology(
                        TechnologyType.STONE_MINING
                )
        );
        assertFalse(townHall.isBusy());
    }

    @Test
    void researchAndUpgradeShouldShareOneCommandQueue() {
        TownHall townHall = createTownHallWithResources(
                100,
                100,
                0
        );

        townHall.startResearch(
                TechnologyType.STONE_MINING
        );

        assertThrows(
                IllegalStateException.class,
                townHall::startUpgrade
        );

        assertThrows(
                IllegalStateException.class,
                () -> townHall.startResearch(
                        TechnologyType.STORAGE_UPGRADE_I
                )
        );
    }

    @Test
    void cancellingResearchShouldNotRefundItsCost() {
        TownHall townHall = createTownHallWithResources(
                50,
                0,
                0
        );

        townHall.startResearch(
                TechnologyType.STONE_MINING
        );
        townHall.cancelCurrentCommand();

        assertFalse(townHall.isBusy());
        assertFalse(
                townHall.hasTechnology(
                        TechnologyType.STONE_MINING
                )
        );
        assertEquals(
                20,
                townHall.getStockpile()
                        .getAmount(ResourceType.WOOD)
        );
    }

    @Test
    void bothStorageUpgradesShouldIncreaseCapacity() {
        TownHall townHall = createTownHallWithResources(
                80,
                60,
                0
        );

        researchAndComplete(
                townHall,
                TechnologyType.STORAGE_UPGRADE_I
        );

        assertEquals(
                150,
                townHall.getStockpile()
                        .getCapacityPerResource()
        );

        researchAndComplete(
                townHall,
                TechnologyType.STORAGE_UPGRADE_II
        );

        assertEquals(
                200,
                townHall.getStockpile()
                        .getCapacityPerResource()
        );
        assertEquals(100, townHall.getStorageTechnologyBonus());
    }

    @Test
    void townHallLevelUpgradeShouldPreserveStorageBonus() {
        TownHall townHall = createTownHallWithResources(
                80,
                70,
                0
        );

        researchAndComplete(
                townHall,
                TechnologyType.STORAGE_UPGRADE_I
        );

        townHall.startUpgrade();
        advanceQueue(townHall, 3);

        assertEquals(TownHallLevel.SETTLEMENT,
                townHall.getCurrentLevel());
        assertEquals(
                250,
                townHall.getStockpile()
                        .getCapacityPerResource()
        );
    }

    @Test
    void ironResearchShouldBeLockedBeforeStoneResearch() {
        TownHall townHall = createTownHallWithResources(
                100,
                100,
                0
        );

        assertFalse(
                townHall.canStartResearch(
                        TechnologyType.IRON_MINING
                )
        );

        assertThrows(
                IllegalStateException.class,
                () -> townHall.startResearch(
                        TechnologyType.IRON_MINING
                )
        );
    }

    private TownHall createTownHallWithResources(
            int wood,
            int stone,
            int iron
    ) {
        TownHall townHall = new TownHall();
        ResourceStockpile stockpile = townHall.getStockpile();

        stockpile.add(ResourceType.WOOD, wood);
        stockpile.add(ResourceType.STONE, stone);
        stockpile.add(ResourceType.IRON, iron);

        return townHall;
    }

    private void researchAndComplete(
            TownHall townHall,
            TechnologyType technologyType
    ) {
        townHall.startResearch(technologyType);
        advanceQueue(
                townHall,
                technologyType.getResearchTurns()
        );
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
