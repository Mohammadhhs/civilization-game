package ir.ac.ap.civilization.model.tribe;

public enum TribeRelationState {
    ENEMY, DISPLEASED, NEUTRAL, FRIENDLY, ALLIED;

    public static TribeRelationState fromScore(int score) {
        if (score <= -51) return ENEMY;
        if (score <= -21) return DISPLEASED;
        if (score < 20) return NEUTRAL;
        if (score < 60) return FRIENDLY;
        return ALLIED;
    }
}
