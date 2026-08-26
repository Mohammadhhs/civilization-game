package ir.ac.ap.civilization.model.trade;

public interface TradeRateStrategy {
    int calculateReceivedAmount(int soldAmount);
}
