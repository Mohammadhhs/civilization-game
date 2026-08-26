package ir.ac.ap.civilization.model.random;

public interface RandomSource {

    int nextInt(int bound);

    long getState();
}
