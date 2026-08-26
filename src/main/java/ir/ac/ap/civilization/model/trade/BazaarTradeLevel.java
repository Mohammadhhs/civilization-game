package ir.ac.ap.civilization.model.trade;

public enum BazaarTradeLevel {
    SMALL(50), MEDIUM(100), LARGE(500);

    private final int soldAmount;

    BazaarTradeLevel(int soldAmount) {
        this.soldAmount = soldAmount;
    }

    public int getSoldAmount() { return soldAmount; }
}
