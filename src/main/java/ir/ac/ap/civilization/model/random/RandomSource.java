package ir.ac.ap.civilization.model.random;

import java.io.Serializable;

public interface RandomSource extends Serializable {

    int nextInt(int bound);

    long getState();
}
