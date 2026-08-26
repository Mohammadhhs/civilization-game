package ir.ac.ap.civilization.model.random;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DeterministicRandomSourceTest {

    @Test
    void equalSeedsShouldProduceEqualSequences() {
        RandomSource first = new DeterministicRandomSource(42L);
        RandomSource second = new DeterministicRandomSource(42L);

        for (int index = 0; index < 20; index++) {
            assertEquals(first.nextInt(1000), second.nextInt(1000));
        }
    }

    @Test
    void capturedStateShouldResumeExactSequence() {
        DeterministicRandomSource original =
                new DeterministicRandomSource(91L);

        original.nextInt(10);
        original.nextInt(10);
        long savedState = original.getState();

        DeterministicRandomSource restored =
                DeterministicRandomSource.fromState(savedState);

        assertEquals(original.nextInt(100), restored.nextInt(100));
        assertEquals(original.nextInt(100), restored.nextInt(100));
        assertThrows(
                IllegalArgumentException.class,
                () -> restored.nextInt(0)
        );
    }
}
