package ir.ac.ap.civilization.model.phase2;

import java.io.Serializable;

public final class HappinessState implements Serializable {

    private int value;

    public int getValue() { return value; }

    public void change(int delta) {
        value = Math.max(-100, Math.min(100, Math.addExact(value, delta)));
    }

    public HappinessLevel getLevel() {
        if (value >= 3) return HappinessLevel.GOLDEN_AGE;
        if (value >= -2) return HappinessLevel.NORMAL;
        if (value >= -6) return HappinessLevel.DISCONTENT;
        return HappinessLevel.REVOLT;
    }

    public double getProductionMultiplier() {
        switch (getLevel()) {
            case GOLDEN_AGE: return 1.25;
            case DISCONTENT: return 0.85;
            case REVOLT: return 0.60;
            default: return 1.0;
        }
    }
}
