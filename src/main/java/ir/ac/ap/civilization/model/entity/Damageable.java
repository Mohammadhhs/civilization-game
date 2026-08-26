package ir.ac.ap.civilization.model.entity;

import java.io.Serializable;

public interface Damageable extends Serializable {

    int getCurrentHitPoints();

    int getMaximumHitPoints();

    void takeDamage(int amount);

    void heal(int amount);

    default boolean isDestroyed() {
        return getCurrentHitPoints() <= 0;
    }
}
