package ir.ac.ap.civilization.model.map;

import java.io.Serializable;

import java.util.Objects;

public final class NaturalResourceDeposit implements Serializable {

    private final NaturalResourceType resourceType;
    private final int initialAmount;

    private int remainingAmount;

    public NaturalResourceDeposit(
            NaturalResourceType resourceType,
            int initialAmount
    ) {
        if (initialAmount <= 0) {
            throw new IllegalArgumentException(
                    "Initial deposit amount must be positive"
            );
        }

        this.resourceType = Objects.requireNonNull(
                resourceType,
                "Natural resource type cannot be null"
        );
        this.initialAmount = initialAmount;
        this.remainingAmount = initialAmount;
    }

    public NaturalResourceType getResourceType() {
        return resourceType;
    }

    public int getInitialAmount() {
        return initialAmount;
    }

    public int getRemainingAmount() {
        return remainingAmount;
    }

    public boolean isDepleted() {
        return remainingAmount == 0;
    }

    public int extract(int requestedAmount) {
        if (requestedAmount <= 0) {
            throw new IllegalArgumentException(
                    "Requested extraction amount must be positive"
            );
        }

        int extractedAmount = Math.min(
                requestedAmount,
                remainingAmount
        );

        remainingAmount -= extractedAmount;
        return extractedAmount;
    }

    @Override
    public String toString() {
        return "NaturalResourceDeposit{" +
                "resourceType=" + resourceType +
                ", initialAmount=" + initialAmount +
                ", remainingAmount=" + remainingAmount +
                '}';
    }
}
