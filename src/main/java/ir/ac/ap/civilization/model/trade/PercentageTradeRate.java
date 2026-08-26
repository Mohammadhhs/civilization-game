package ir.ac.ap.civilization.model.trade;

public final class PercentageTradeRate implements TradeRateStrategy {

    private final int percentage;

    public PercentageTradeRate(int percentage) {
        if (percentage <= 0 || percentage > 100) {
            throw new IllegalArgumentException("Percentage must be 1..100");
        }
        this.percentage = percentage;
    }

    @Override
    public int calculateReceivedAmount(int soldAmount) {
        if (soldAmount <= 0) {
            throw new IllegalArgumentException("Sold amount must be positive");
        }
        return Math.multiplyExact(soldAmount, percentage) / 100;
    }
}
