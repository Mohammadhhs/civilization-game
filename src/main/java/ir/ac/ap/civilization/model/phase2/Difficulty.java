package ir.ac.ap.civilization.model.phase2;

public enum Difficulty {
    EASY("Easy", 8, 0.75),
    NORMAL("Normal", 15, 1.0),
    HARD("Hard", 24, 1.25);

    private final String displayName;
    private final int disasterChancePercent;
    private final double enemyStrengthMultiplier;

    Difficulty(String displayName, int disasterChancePercent,
               double enemyStrengthMultiplier) {
        this.displayName = displayName;
        this.disasterChancePercent = disasterChancePercent;
        this.enemyStrengthMultiplier = enemyStrengthMultiplier;
    }
    public int getDisasterChancePercent() { return disasterChancePercent; }
    public double getEnemyStrengthMultiplier() { return enemyStrengthMultiplier; }
    @Override public String toString() { return displayName; }
}
