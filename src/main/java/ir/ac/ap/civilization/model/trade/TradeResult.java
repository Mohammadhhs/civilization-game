package ir.ac.ap.civilization.model.trade;

import ir.ac.ap.civilization.model.resource.ResourceType;

public final class TradeResult {
    private final ResourceType soldResource;
    private final int soldAmount;
    private final ResourceType receivedResource;
    private final int receivedAmount;

    public TradeResult(ResourceType soldResource, int soldAmount,
                       ResourceType receivedResource, int receivedAmount) {
        this.soldResource = soldResource;
        this.soldAmount = soldAmount;
        this.receivedResource = receivedResource;
        this.receivedAmount = receivedAmount;
    }
    public ResourceType getSoldResource() { return soldResource; }
    public int getSoldAmount() { return soldAmount; }
    public ResourceType getReceivedResource() { return receivedResource; }
    public int getReceivedAmount() { return receivedAmount; }
}
