package ir.ac.ap.civilization.model.entity;

public interface Damageable {

    int getCurrentHitPoints();

    int getMaximumHitPoints();

    void takeDamage(int amount);

    void heal(int amount);

    default boolean isDestroyed() {
        return getCurrentHitPoints() <= 0;
    }
}
