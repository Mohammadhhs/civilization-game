package ir.ac.ap.civilization.model.random;

public final class DeterministicRandomSource
        implements RandomSource {

    private static final long NON_ZERO_FALLBACK =
            0x9E3779B97F4A7C15L;

    private long state;

    public DeterministicRandomSource(long seed) {
        this.state = normalize(seed);
    }

    public static DeterministicRandomSource fromState(long state) {
        return new DeterministicRandomSource(state);
    }

    @Override
    public int nextInt(int bound) {
        if (bound <= 0) {
            throw new IllegalArgumentException(
                    "Random bound must be positive"
            );
        }

        long value = nextLong();
        return (int) Long.remainderUnsigned(value, bound);
    }

    @Override
    public long getState() {
        return state;
    }

    private long nextLong() {
        long value = state;
        value ^= value >>> 12;
        value ^= value << 25;
        value ^= value >>> 27;
        state = value;
        return value * 2685821657736338717L;
    }

    private static long normalize(long value) {
        return value == 0L ? NON_ZERO_FALLBACK : value;
    }
}
