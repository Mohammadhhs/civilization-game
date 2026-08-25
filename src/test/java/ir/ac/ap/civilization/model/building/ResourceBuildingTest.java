package ir.ac.ap.civilization.model.building;

import ir.ac.ap.civilization.model.map.HexCoordinate;
import ir.ac.ap.civilization.model.map.NaturalResourceType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResourceBuildingTest {

    @Test
    void buildingShouldEnforceWorkerCapacity() {
        ResourceBuilding building = new ResourceBuilding(
                1,
                BuildingType.LUMBER_MILL,
                new HexCoordinate(1, 0),
                NaturalResourceType.WOOD
        );

        building.stationWorker(3);
        building.stationWorker(4);

        assertEquals(2, building.getStationedWorkerCount());
        assertFalse(building.hasWorkerCapacity());

        assertThrows(
                IllegalStateException.class,
                () -> building.stationWorker(5)
        );
    }

    @Test
    void buildingShouldRejectIncompatibleResource() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new ResourceBuilding(
                        1,
                        BuildingType.FARM,
                        new HexCoordinate(1, 0),
                        NaturalResourceType.WOOD
                )
        );
    }

    @Test
    void removingWorkerShouldFreeCapacity() {
        ResourceBuilding building = new ResourceBuilding(
                1,
                BuildingType.STONE_MINE,
                new HexCoordinate(1, 0),
                NaturalResourceType.STONE
        );

        building.stationWorker(3);
        building.removeWorker(3);

        assertEquals(0, building.getStationedWorkerCount());
        assertTrue(building.hasWorkerCapacity());
    }

    @Test
    void paidUpkeepShouldResetConsecutiveMisses() {
        ResourceBuilding building = new ResourceBuilding(
                1,
                BuildingType.LUMBER_MILL,
                new HexCoordinate(1, 0),
                NaturalResourceType.WOOD
        );

        building.recordMissedUpkeep();
        building.recordMissedUpkeep();

        assertTrue(building.isAtRiskFromUpkeep());
        assertFalse(building.shouldBeDestroyedForUpkeep());

        building.recordUpkeepPaid();

        assertEquals(0, building.getConsecutiveMissedUpkeepTurns());
        assertFalse(building.isAtRiskFromUpkeep());
    }

    @Test
    void thirdMissedUpkeepShouldMarkBuildingForDestruction() {
        ResourceBuilding building = new ResourceBuilding(
                1,
                BuildingType.LUMBER_MILL,
                new HexCoordinate(1, 0),
                NaturalResourceType.WOOD
        );

        building.recordMissedUpkeep();
        building.recordMissedUpkeep();
        building.recordMissedUpkeep();

        assertTrue(building.shouldBeDestroyedForUpkeep());
    }
}
