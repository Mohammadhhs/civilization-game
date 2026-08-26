package ir.ac.ap.civilization.model.trade;

import ir.ac.ap.civilization.model.game.GameState;
import ir.ac.ap.civilization.model.resource.ResourceStockpile;
import ir.ac.ap.civilization.model.resource.ResourceType;

import java.util.Objects;

public final class TradeService {

    private final GameState gameState;

    public TradeService(GameState gameState) {
        this.gameState = Objects.requireNonNull(gameState, "Game state cannot be null");
    }

    public TradeResult tradeAtBazaar(
            ResourceType sold,
            ResourceType received,
            BazaarTradeLevel level
    ) {
        Objects.requireNonNull(level, "Trade level cannot be null");
        return execute("bazaar", sold, received, level.getSoldAmount(),
                new PercentageTradeRate(60));
    }

    public TradeResult tradeAtPost(
            ResourceType sold,
            ResourceType received,
            int amount
    ) {
        return execute("trading-post", sold, received, amount,
                new PercentageTradeRate(70));
    }

    public TradeResult execute(
            String sourceKey,
            ResourceType sold,
            ResourceType received,
            int amount,
            TradeRateStrategy rate
    ) {
        Objects.requireNonNull(sourceKey, "Trade source cannot be null");
        Objects.requireNonNull(sold, "Sold resource cannot be null");
        Objects.requireNonNull(received, "Received resource cannot be null");
        Objects.requireNonNull(rate, "Trade rate cannot be null");
        if (sold == received) {
            throw new IllegalArgumentException("Trade resources must differ");
        }
        if (gameState.wasTradeUsedThisTurn(sourceKey)) {
            throw new IllegalStateException("This trade source was already used this turn");
        }
        int receivedAmount = rate.calculateReceivedAmount(amount);
        ResourceStockpile stockpile = gameState.getTownHall().getStockpile();
        if (!stockpile.hasEnough(sold, amount)) {
            throw new IllegalStateException("Not enough resource to trade");
        }
        int freeCapacity = stockpile.getCapacityPerResource() -
                stockpile.getAmount(received);
        if (receivedAmount > freeCapacity) {
            throw new IllegalStateException("Not enough storage capacity for trade");
        }
        stockpile.spend(sold, amount);
        stockpile.add(received, receivedAmount);
        gameState.recordTrade(sourceKey);
        return new TradeResult(sold, amount, received, receivedAmount);
    }
}
