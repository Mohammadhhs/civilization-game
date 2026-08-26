package ir.ac.ap.civilization.model.resource;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.io.Serializable;

public final class ResourceStockpile implements Serializable {

    private final EnumMap<ResourceType, Integer> amounts;
    private int capacityPerResource;

    public ResourceStockpile(int capacityPerResource) {
        validateCapacity(capacityPerResource);

        this.capacityPerResource = capacityPerResource;
        this.amounts = new EnumMap<>(ResourceType.class);

        for (ResourceType resourceType : ResourceType.values()) {
            amounts.put(resourceType, 0);
        }
    }

    public int getAmount(ResourceType resourceType) {
        Objects.requireNonNull(
                resourceType,
                "Resource type cannot be null"
        );

        return amounts.get(resourceType);
    }

    public int getCapacityPerResource() {
        return capacityPerResource;
    }

    public void setCapacityPerResource(int capacityPerResource) {
        validateCapacity(capacityPerResource);
        this.capacityPerResource = capacityPerResource;
    }

    public void add(ResourceType resourceType, int amount) {
        Objects.requireNonNull(
                resourceType,
                "Resource type cannot be null"
        );

        validateNonNegativeAmount(amount);

        int currentAmount = amounts.get(resourceType);
        int newAmount = Math.addExact(currentAmount, amount);

        amounts.put(resourceType, newAmount);
    }

    public boolean hasEnough(ResourceType resourceType, int requiredAmount) {
        Objects.requireNonNull(
                resourceType,
                "Resource type cannot be null"
        );

        validateNonNegativeAmount(requiredAmount);

        return amounts.get(resourceType) >= requiredAmount;
    }

    public boolean canAfford(Map<ResourceType, Integer> costs) {
        validateCosts(costs);
        return canAffordValidatedCosts(costs);
    }

    public void spend(ResourceType resourceType, int amount) {
        Objects.requireNonNull(
                resourceType,
                "Resource type cannot be null"
        );

        validateNonNegativeAmount(amount);

        if (!hasEnough(resourceType, amount)) {
            throw new IllegalStateException(
                    "Not enough " + resourceType +
                            ". Required: " + amount +
                            ", available: " + getAmount(resourceType)
            );
        }

        amounts.put(
                resourceType,
                amounts.get(resourceType) - amount
        );
    }

    public void spend(Map<ResourceType, Integer> costs) {
        validateCosts(costs);

        if (!canAffordValidatedCosts(costs)) {
            throw new IllegalStateException(
                    "Not enough resources to pay all costs"
            );
        }

        for (Map.Entry<ResourceType, Integer> entry : costs.entrySet()) {
            ResourceType resourceType = entry.getKey();
            int cost = entry.getValue();

            amounts.put(
                    resourceType,
                    amounts.get(resourceType) - cost
            );
        }
    }

    public boolean isOverCapacity(ResourceType resourceType) {
        Objects.requireNonNull(
                resourceType,
                "Resource type cannot be null"
        );

        return amounts.get(resourceType) > capacityPerResource;
    }

    public void applyCapacityLimit() {
        for (ResourceType resourceType : ResourceType.values()) {
            int currentAmount = amounts.get(resourceType);

            if (currentAmount > capacityPerResource) {
                amounts.put(resourceType, capacityPerResource);
            }
        }
    }

    public Map<ResourceType, Integer> getAmounts() {
        return Collections.unmodifiableMap(
                new EnumMap<>(amounts)
        );
    }

    private boolean canAffordValidatedCosts(
            Map<ResourceType, Integer> costs
    ) {
        for (Map.Entry<ResourceType, Integer> entry : costs.entrySet()) {
            ResourceType resourceType = entry.getKey();
            int requiredAmount = entry.getValue();

            if (amounts.get(resourceType) < requiredAmount) {
                return false;
            }
        }

        return true;
    }

    private void validateCosts(Map<ResourceType, Integer> costs) {
        Objects.requireNonNull(costs, "Costs cannot be null");

        for (Map.Entry<ResourceType, Integer> entry : costs.entrySet()) {
            Objects.requireNonNull(
                    entry.getKey(),
                    "Cost resource type cannot be null"
            );

            Integer amount = Objects.requireNonNull(
                    entry.getValue(),
                    "Cost amount cannot be null"
            );

            validateNonNegativeAmount(amount);
        }
    }

    private void validateCapacity(int capacity) {
        if (capacity < 0) {
            throw new IllegalArgumentException(
                    "Capacity cannot be negative"
            );
        }
    }

    private void validateNonNegativeAmount(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException(
                    "Resource amount cannot be negative"
            );
        }
    }
}
