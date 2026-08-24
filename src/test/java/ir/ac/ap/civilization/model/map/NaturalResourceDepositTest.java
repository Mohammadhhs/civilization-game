package ir.ac.ap.civilization.model.map;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NaturalResourceDepositTest {

    @Test
    void extractionShouldReduceRemainingAmount() {
        NaturalResourceDeposit deposit =
                new NaturalResourceDeposit(
                        NaturalResourceType.WOOD,
                        120
                );

        assertEquals(35, deposit.extract(35));
        assertEquals(120, deposit.getInitialAmount());
        assertEquals(85, deposit.getRemainingAmount());
        assertFalse(deposit.isDepleted());
    }

    @Test
    void extractionShouldNeverMakeDepositNegative() {
        NaturalResourceDeposit deposit =
                new NaturalResourceDeposit(
                        NaturalResourceType.IRON,
                        45
                );

        assertEquals(45, deposit.extract(100));
        assertEquals(0, deposit.getRemainingAmount());
        assertTrue(deposit.isDepleted());
        assertEquals(0, deposit.extract(10));
    }

    @Test
    void invalidAmountsShouldBeRejected() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new NaturalResourceDeposit(
                        NaturalResourceType.STONE,
                        0
                )
        );

        NaturalResourceDeposit deposit =
                new NaturalResourceDeposit(
                        NaturalResourceType.STONE,
                        90
                );

        assertThrows(
                IllegalArgumentException.class,
                () -> deposit.extract(0)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> deposit.extract(-1)
        );
    }
}