package ir.ac.ap.civilization.model.entity;

import java.io.Serializable;

public final class HitPoints implements Serializable {

    private int current;
    private int maximum;

    public HitPoints(int maximum) {
        this(maximum, maximum);
    }

    public HitPoints(int current, int maximum) {
        if (maximum <= 0) {
            throw new IllegalArgumentException(
                    "Maximum hit points must be positive"
            );
        }
        if (current < 0 || current > maximum) {
            throw new IllegalArgumentException(
                    "Current hit points must be between zero and maximum"
            );
        }
        this.current = current;
        this.maximum = maximum;
    }

    public int getCurrent() {
        return current;
    }

    public int getMaximum() {
        return maximum;
    }

    public boolean isDepleted() {
        return current == 0;
    }

    public void damage(int amount) {
        validateNonNegative(amount, "Damage");
        current = Math.max(0, current - amount);
    }

    public void heal(int amount) {
        validateNonNegative(amount, "Heal");
        current = Math.min(maximum, current + amount);
    }

    public void increaseMaximum(int amount, boolean healIncrease) {
        validateNonNegative(amount, "Maximum hit point increase");
        maximum = Math.addExact(maximum, amount);
        if (healIncrease) {
            current = Math.addExact(current, amount);
        }
    }

    private void validateNonNegative(
            int amount,
            String operationName
    ) {
        if (amount < 0) {
            throw new IllegalArgumentException(
                    operationName + " amount cannot be negative"
            );
        }
    }
}
