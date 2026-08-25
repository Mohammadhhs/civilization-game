package ir.ac.ap.civilization.model.unit;

import ir.ac.ap.civilization.model.map.HexCoordinate;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UnitTest {

    @Test
    void civilianUnitsStartWithTheirConfiguredStatistics() {
        HexCoordinate position = new HexCoordinate(1, 0);

        Builder builder = new Builder(1, position);
        Worker worker = new Worker(2, position);
        Explorer explorer = new Explorer(3, position);

        assertEquals(UnitType.BUILDER, builder.getUnitType());
        assertEquals(5, builder.getMaximumActionPoints());
        assertEquals(1, builder.getVisionRadius());

        assertEquals(UnitType.WORKER, worker.getUnitType());
        assertEquals(4, worker.getMaximumActionPoints());
        assertEquals(1, worker.getVisionRadius());

        assertEquals(UnitType.EXPLORER, explorer.getUnitType());
        assertEquals(8, explorer.getMaximumActionPoints());
        assertEquals(3, explorer.getVisionRadius());
    }

    @Test
    void spendingActionPointsReducesOnlyCurrentAmount() {
        Explorer explorer = new Explorer(
                1,
                new HexCoordinate(1, 0)
        );

        explorer.spendActionPoints(3);

        assertEquals(5, explorer.getCurrentActionPoints());
        assertEquals(8, explorer.getMaximumActionPoints());
        assertTrue(explorer.hasActionPoints());
    }

    @Test
    void failedActionPointPaymentDoesNotChangeAmount() {
        Worker worker = new Worker(
                1,
                new HexCoordinate(1, 0)
        );

        assertThrows(
                IllegalStateException.class,
                () -> worker.spendActionPoints(5)
        );

        assertEquals(4, worker.getCurrentActionPoints());
    }

    @Test
    void negativeActionPointCostIsRejected() {
        Worker worker = new Worker(
                1,
                new HexCoordinate(1, 0)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> worker.spendActionPoints(-1)
        );

        assertEquals(4, worker.getCurrentActionPoints());
    }

    @Test
    void actionPointsCanBeRestoredForANewTurn() {
        Builder builder = new Builder(
                1,
                new HexCoordinate(1, 0)
        );

        builder.spendActionPoints(5);

        assertFalse(builder.hasActionPoints());

        builder.restoreActionPoints();

        assertEquals(5, builder.getCurrentActionPoints());
        assertTrue(builder.hasActionPoints());
    }

    @Test
    void builderChargesAreConsumedWithoutBecomingNegative() {
        Builder builder = new Builder(
                1,
                new HexCoordinate(1, 0)
        );

        assertEquals(3, builder.getRemainingCharges());

        builder.useCharge();
        builder.useCharge();
        builder.useCharge();

        assertEquals(0, builder.getRemainingCharges());
        assertFalse(builder.hasCharges());

        assertThrows(
                IllegalStateException.class,
                builder::useCharge
        );

        assertEquals(0, builder.getRemainingCharges());
    }

    @Test
    void unitIdentityAndPositionAreAvailable() {
        HexCoordinate position = new HexCoordinate(-1, 1);
        Explorer explorer = new Explorer(7, position);

        assertEquals(7, explorer.getId());
        assertEquals(position, explorer.getPosition());
        assertEquals("Explorer", explorer.getDisplayName());
        assertEquals("E", explorer.getMapSymbol());
    }

    @Test
    void workerShouldRememberItsStationedBuilding() {
        Worker worker = new Worker(
                1,
                new HexCoordinate(1, 0)
        );

        worker.stationAt(7);

        assertTrue(worker.isStationed());
        assertEquals(7, worker.getStationedBuildingId());

        worker.leaveBuilding();

        assertFalse(worker.isStationed());
    }

    @Test
    void actionPointPenaltyShouldNotMakePointsNegative() {
        Worker worker = new Worker(
                1,
                new HexCoordinate(1, 0)
        );

        worker.applyActionPointPenalty(2);
        assertEquals(2, worker.getCurrentActionPoints());

        worker.applyActionPointPenalty(10);
        assertEquals(0, worker.getCurrentActionPoints());
    }
}
