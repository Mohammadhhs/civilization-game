package ir.ac.ap.civilization.model.building;

import ir.ac.ap.civilization.model.entity.Damageable;
import ir.ac.ap.civilization.model.entity.EntityKey;
import ir.ac.ap.civilization.model.entity.EntityKind;
import ir.ac.ap.civilization.model.entity.GameEntity;
import ir.ac.ap.civilization.model.entity.Owner;
import ir.ac.ap.civilization.model.entity.OwnedEntity;
import ir.ac.ap.civilization.model.production.ProductionCommand;
import ir.ac.ap.civilization.model.production.UnitProductionCommand;
import ir.ac.ap.civilization.model.resource.ResourceStockpile;
import ir.ac.ap.civilization.model.resource.ResourceType;
import ir.ac.ap.civilization.model.technology.TechnologyManager;
import ir.ac.ap.civilization.model.technology.TechnologyType;
import ir.ac.ap.civilization.model.unit.UnitType;

import java.util.Objects;
import java.util.Optional;

public final class TownHall implements
        GameEntity, OwnedEntity, Damageable {

    private static final int BASE_MAXIMUM_HP = 200;
    private static final int BASE_FOOD_PRODUCTION = 4;
    private static final int BASE_WOOD_PRODUCTION = 4;

    private final ResourceStockpile stockpile;
    private final TechnologyManager technologyManager;

    private TownHallLevel currentLevel;
    private int currentHp;
    private int maximumHp;
    private int storageTechnologyBonus;
    private ProductionCommand activeCommand;

    public TownHall() {
        this.currentLevel = TownHallLevel.BASE_CAMP;
        this.maximumHp = BASE_MAXIMUM_HP;
        this.currentHp = maximumHp;
        this.storageTechnologyBonus = 0;

        this.stockpile = new ResourceStockpile(
                currentLevel.getStorageCapacity()
        );

        this.technologyManager =
                new TechnologyManager();
    }

    @Override
    public int getId() {
        return 1;
    }

    @Override
    public EntityKey getEntityKey() {
        return new EntityKey(EntityKind.TOWN_HALL, getId());
    }

    @Override
    public Owner getOwner() {
        return Owner.PLAYER;
    }

    public TownHallLevel getCurrentLevel() {
        return currentLevel;
    }

    public int getCurrentHp() {
        return currentHp;
    }

    @Override
    public int getCurrentHitPoints() {
        return getCurrentHp();
    }

    public int getMaximumHp() {
        return maximumHp;
    }

    @Override
    public int getMaximumHitPoints() {
        return getMaximumHp();
    }

    public ResourceStockpile getStockpile() {
        return stockpile;
    }

    public TechnologyManager getTechnologyManager() {
        return technologyManager;
    }

    public int getStorageTechnologyBonus() {
        return storageTechnologyBonus;
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

    public boolean canStartResearch(
            TechnologyType technologyType
    ) {
        Objects.requireNonNull(
                technologyType,
                "Technology type cannot be null"
        );

        return !isBusy() &&
                currentLevel.getLevelNumber() >=
                        technologyType.getMinimumTownHallLevel() &&
                technologyManager.canResearch(technologyType) &&
                stockpile.canAfford(
                        technologyType.getResearchCost()
                );
    }

    public boolean hasTechnology(
            TechnologyType technologyType
    ) {
        return technologyManager.isCompleted(technologyType);
    }

    public Optional<TechnologyType>
    getActiveResearchTechnology() {
        if (!(activeCommand instanceof
                TechnologyResearchCommand)) {
            return Optional.empty();
        }

        TechnologyResearchCommand command =
                (TechnologyResearchCommand) activeCommand;

        return Optional.of(command.getTechnologyType());
    }

    public double getMiningProductionMultiplier() {
        return technologyManager
                .getMiningProductionMultiplier();
    }

    public boolean canStartUnitProduction(UnitType unitType) {
        Objects.requireNonNull(
                unitType,
                "Unit type cannot be null"
        );

        return !isBusy() &&
                stockpile.canAfford(
                        unitType.getProductionCost()
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

    public void startResearch(
            TechnologyType technologyType
    ) {
        Objects.requireNonNull(
                technologyType,
                "Technology type cannot be null"
        );

        if (isBusy()) {
            throw new IllegalStateException(
                    "Town Hall is already processing a command"
            );
        }

        if (technologyManager.isCompleted(technologyType)) {
            throw new IllegalStateException(
                    "Technology has already been researched"
            );
        }

        if (currentLevel.getLevelNumber() <
                technologyType.getMinimumTownHallLevel()) {
            throw new IllegalStateException(
                    "Town Hall level is too low for this technology"
            );
        }

        if (!technologyManager
                .arePrerequisitesCompleted(technologyType)) {
            throw new IllegalStateException(
                    "Technology prerequisites are not completed"
            );
        }

        if (!stockpile.canAfford(
                technologyType.getResearchCost()
        )) {
            throw new IllegalStateException(
                    "Not enough resources to research technology"
            );
        }

        stockpile.spend(
                technologyType.getResearchCost()
        );

        activeCommand =
                new TechnologyResearchCommand(technologyType);
    }

    public void startUnitProduction(UnitType unitType) {
        Objects.requireNonNull(
                unitType,
                "Unit type cannot be null"
        );

        if (isBusy()) {
            throw new IllegalStateException(
                    "Town Hall is already processing a command"
            );
        }

        if (!stockpile.canAfford(
                unitType.getProductionCost()
        )) {
            throw new IllegalStateException(
                    "Not enough resources to train unit"
            );
        }

        stockpile.spend(unitType.getProductionCost());
        activeCommand = new UnitProductionCommand(unitType);
    }

    public ProductionCommand advanceProductionQueue() {
        if (activeCommand == null) {
            return null;
        }

        activeCommand.advanceTurn();

        if (activeCommand.isCompleted()) {
            ProductionCommand completedCommand = activeCommand;
            completedCommand.complete(this);
            activeCommand = null;
            return completedCommand;
        }

        return null;
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
                targetLevel.getStorageCapacity() +
                        storageTechnologyBonus
        );

        heal(targetLevel.getHealAfterUpgrade());
    }

    void applyTechnology(
            TechnologyType technologyType
    ) {
        technologyManager.completeResearch(technologyType);

        if (technologyType == TechnologyType.DEFENSIVE_ARCHITECTURE) {
            maximumHp = Math.max(maximumHp, 250);
            currentHp = Math.min(maximumHp, currentHp + 50);
        }

        storageTechnologyBonus +=
                technologyType.getStorageCapacityBonus();

        stockpile.setCapacityPerResource(
                currentLevel.getStorageCapacity() +
                        storageTechnologyBonus
        );
    }
}
