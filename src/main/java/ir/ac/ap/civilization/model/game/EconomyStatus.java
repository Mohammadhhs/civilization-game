package ir.ac.ap.civilization.model.game;

import java.io.Serializable;

public final class EconomyStatus implements Serializable {

    private boolean starving;
    private int requiredUnitFood;
    private int paidUnitFood;
    private int buildingsAtRisk;
    private int buildingsDestroyedLastTurn;

    public EconomyStatus() {
        starving = false;
        requiredUnitFood = 0;
        paidUnitFood = 0;
        buildingsAtRisk = 0;
        buildingsDestroyedLastTurn = 0;
    }

    public boolean isStarving() {
        return starving;
    }

    public boolean canCityGrow() {
        return !starving;
    }

    public int getRequiredUnitFood() {
        return requiredUnitFood;
    }

    public int getPaidUnitFood() {
        return paidUnitFood;
    }

    public int getFoodDeficit() {
        return requiredUnitFood - paidUnitFood;
    }

    public int getBuildingsAtRisk() {
        return buildingsAtRisk;
    }

    public int getBuildingsDestroyedLastTurn() {
        return buildingsDestroyedLastTurn;
    }

    public boolean hasResourceCrisis() {
        return starving ||
                buildingsAtRisk > 0 ||
                buildingsDestroyedLastTurn > 0;
    }

    void update(
            boolean starving,
            int requiredUnitFood,
            int paidUnitFood,
            int buildingsAtRisk,
            int buildingsDestroyedLastTurn
    ) {
        this.starving = starving;
        this.requiredUnitFood = requiredUnitFood;
        this.paidUnitFood = paidUnitFood;
        this.buildingsAtRisk = buildingsAtRisk;
        this.buildingsDestroyedLastTurn =
                buildingsDestroyedLastTurn;
    }
}
