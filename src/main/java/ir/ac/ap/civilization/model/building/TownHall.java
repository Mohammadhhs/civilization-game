package ir.ac.ap.civilization.model.building;

import ir.ac.ap.civilization.model.production.ProductionCommand;
import ir.ac.ap.civilization.model.resource.ResourceStockpile;
import ir.ac.ap.civilization.model.resource.ResourceType;

import java.util.Objects;

public final class TownHall {

    private static final int BASE_MAXIMUM_HP = 200;
    private static final int BASE_FOOD_PRODUCTION = 4;
    private static final int BASE_WOOD_PRODUCTION = 4;

    private final ResourceStockpile stockpile;

    private TownHallLevel currentLevel;
    private int currentHp;
    private int maximumHp;
    private ProductionCommand activeCommand;

    public TownHall() {
        this.currentLevel = TownHallLevel.BASE_CAMP;
        this.maximumHp = BASE_MAXIMUM_HP;
        this.currentHp = maximumHp;

        this.stockpile = new ResourceStockpile(
                currentLevel.getStorageCapacity()
        );
    }

    public TownHallLevel getCurrentLevel() {
        return currentLevel;
    }

    public int getCurrentHp() {
        return currentHp;
    }

    public int getMaximumHp() {
        return maximumHp;
    }

    public ResourceStockpile getStockpile() {
        return stockpile;
    }

    public boolean isBusy() {
        return activeCommand != null;
    }

    public boolean canStartUpgrade() {
        if (isBusy() || currentLevel.isMaximumLevel()) {
            return false;
        }

        TownHallLevel targetLevel =
                currentLevel.getNextLevel();

        return stockpile.canAfford(
                targetLevel.getUpgradeCost()
        );
    }

    public int getBaseProduction(
            ResourceType resourceType
    ) {
        Objects.requireNonNull(
                resourceType,
                "Resource type cannot be null"
        );

        switch (resourceType) {
            case FOOD:
                return BASE_FOOD_PRODUCTION;

            case WOOD:
                return BASE_WOOD_PRODUCTION;

            case STONE:
            case IRON:
                return 0;

            default:
                throw new IllegalStateException(
                        "Unknown resource type"
                );
        }
    }

    public String getActiveCommandName() {
        if (activeCommand == null) {
            return "Idle";
        }

        return activeCommand.getName();
    }

    public int getRemainingProductionTurns() {
        if (activeCommand == null) {
            return 0;
        }

        return activeCommand.getRemainingTurns();
    }

    public void produceBaseResources() {
        for (ResourceType resourceType :
                ResourceType.values()) {

            int production =
                    getBaseProduction(resourceType);

            if (production > 0) {
                stockpile.add(
                        resourceType,
                        production
                );
            }
        }
    }

    public void startUpgrade() {
        if (isBusy()) {
            throw new IllegalStateException(
                    "Town Hall is already processing a command"
            );
        }

        if (currentLevel.isMaximumLevel()) {
            throw new IllegalStateException(
                    "Town Hall is already at maximum level"
            );
        }

        TownHallLevel targetLevel =
                currentLevel.getNextLevel();

        if (!stockpile.canAfford(
                targetLevel.getUpgradeCost()
        )) {
            throw new IllegalStateException(
                    "Not enough resources to upgrade Town Hall"
            );
        }

        stockpile.spend(targetLevel.getUpgradeCost());

        activeCommand =
                new TownHallUpgradeCommand(targetLevel);
    }

    public void advanceProductionQueue() {
        if (activeCommand == null) {
            return;
        }

        activeCommand.advanceTurn();

        if (activeCommand.isCompleted()) {
            activeCommand.complete(this);
            activeCommand = null;
        }
    }

    public void cancelCurrentCommand() {
        if (activeCommand == null) {
            throw new IllegalStateException(
                    "Town Hall has no active command to cancel"
            );
        }

        activeCommand = null;
    }

    public void takeDamage(int damage) {
        if (damage < 0) {
            throw new IllegalArgumentException(
                    "Damage cannot be negative"
            );
        }

        currentHp = Math.max(
                0,
                currentHp - damage
        );
    }

    public void heal(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException(
                    "Heal amount cannot be negative"
            );
        }

        currentHp = Math.min(
                maximumHp,
                currentHp + amount
        );
    }

    void applyUpgrade(TownHallLevel targetLevel) {
        Objects.requireNonNull(
                targetLevel,
                "Target level cannot be null"
        );

        TownHallLevel expectedLevel =
                currentLevel.getNextLevel();

        if (targetLevel != expectedLevel) {
            throw new IllegalStateException(
                    "Invalid Town Hall upgrade transition"
            );
        }

        currentLevel = targetLevel;

        stockpile.setCapacityPerResource(
                targetLevel.getStorageCapacity()
        );

        heal(targetLevel.getHealAfterUpgrade());
    }
}