package ir.ac.ap.civilization.model.tribe;

import ir.ac.ap.civilization.model.game.GameState;
import ir.ac.ap.civilization.model.resource.ResourceStockpile;
import ir.ac.ap.civilization.model.resource.ResourceType;
import ir.ac.ap.civilization.model.trade.PercentageTradeRate;
import ir.ac.ap.civilization.model.trade.TradeResult;
import ir.ac.ap.civilization.model.trade.TradeService;

import java.util.Objects;

public final class TribeService {
    private final GameState gameState;
    private final TradeService tradeService;

    public TribeService(GameState gameState) {
        this.gameState = Objects.requireNonNull(gameState, "Game state cannot be null");
        this.tradeService = new TradeService(gameState);
    }

    public void sendGift(int tribeId, ResourceType resource, int amount) {
        Tribe tribe = requireTribe(tribeId);
        if (tribe.getRelationState() == TribeRelationState.ENEMY) {
            throw new IllegalStateException("Cannot gift an enemy tribe");
        }
        if (amount <= 0) throw new IllegalArgumentException("Gift must be positive");
        gameState.getTownHall().getStockpile().spend(resource, amount);
        tribe.changeRelation(Math.max(1, amount / 10));
        gameState.getPhaseTwoState().notifyPlayer("Gift sent to " + tribe.getName());
    }

    public TradeResult trade(int tribeId, ResourceType sold,
                             ResourceType received, int amount) {
        Tribe tribe = requireTribe(tribeId);
        TribeRelationState relation = tribe.getRelationState();
        if (relation != TribeRelationState.FRIENDLY &&
                relation != TribeRelationState.ALLIED) {
            throw new IllegalStateException("Trade requires a friendly tribe");
        }
        validateTradeResource(tribe, received);
        int percent = tribe.getType() == TribeType.MERCHANT ? 80 : 65;
        TradeResult result = tradeService.execute(
                "tribe-" + tribeId, sold, received, amount,
                new PercentageTradeRate(percent)
        );
        tribe.changeRelation(2);
        return result;
    }

    public void acceptMission(int tribeId) {
        TribeMission mission = requireTribe(tribeId).getMission()
                .orElseThrow(() -> new IllegalStateException("No mission is offered"));
        mission.activate();
    }

    public void completeMission(int tribeId) {
        Tribe tribe = requireTribe(tribeId);
        TribeMission mission = tribe.getMission()
                .orElseThrow(() -> new IllegalStateException("No active mission"));
        if (mission.getStatus() != MissionStatus.ACTIVE) {
            throw new IllegalStateException("Mission is not active");
        }
        ResourceStockpile stockpile = gameState.getTownHall().getStockpile();
        stockpile.spend(mission.getRequiredResource(), mission.getRequiredAmount());
        mission.complete();
        tribe.changeRelation(mission.getRelationReward());
        gameState.getPhaseTwoState().notifyPlayer("Mission completed: " + mission.getTitle());
    }

    public void declareWar(int tribeId) {
        Tribe tribe = requireTribe(tribeId);
        if (tribe.getRelationState() == TribeRelationState.ALLIED) {
            gameState.getPhaseTwoState().getHappiness().change(-15);
        } else if (tribe.getRelationState() == TribeRelationState.FRIENDLY) {
            gameState.getPhaseTwoState().getHappiness().change(-5);
        }
        tribe.declareWar();
    }

    public void requestPeace(int tribeId) {
        Tribe tribe = requireTribe(tribeId);
        if (tribe.getRelationState() != TribeRelationState.ENEMY) {
            throw new IllegalStateException("Peace is only available during war");
        }
        ResourceStockpile stockpile = gameState.getTownHall().getStockpile();
        stockpile.spend(ResourceType.FOOD, 30);
        stockpile.spend(ResourceType.WOOD, 30);
        stockpile.spend(ResourceType.IRON, 30);
        tribe.makePeace();
    }

    public void requestAlliance(int tribeId) {
        Tribe tribe = requireTribe(tribeId);
        if (tribe.getRelation() < 60) {
            throw new IllegalStateException("Alliance requires relation 60");
        }
        if (tribe.getType() == TribeType.WARRIOR &&
                gameState.hasAlliedTribe(TribeType.FARMER) ||
                tribe.getType() == TribeType.FARMER &&
                        gameState.hasAlliedTribe(TribeType.WARRIOR)) {
            throw new IllegalStateException("These tribes refuse a shared alliance");
        }
        tribe.changeRelation(100 - tribe.getRelation());
    }

    public void advanceTurn() {
        for (Tribe tribe : gameState.getTribes()) {
            if (tribe.isDefeated()) continue;
            if (tribe.getMission().isPresent()) {
                tribe.getMission().get().advanceTurn();
            } else if (tribe.getRelationState() == TribeRelationState.FRIENDLY &&
                    gameState.getTurnNumber() % 5 == 0) {
                tribe.setMission(createMission(tribe));
                gameState.getPhaseTwoState().notifyPlayer(
                        tribe.getName() + " offers a new mission"
                );
            }
        }
    }

    private TribeMission createMission(Tribe tribe) {
        ResourceType resource = tribe.getType() == TribeType.MOUNTAIN
                ? ResourceType.WOOD : ResourceType.STONE;
        return new TribeMission("Aid " + tribe.getName(), resource, 20, 5, 15);
    }

    private void validateTradeResource(Tribe tribe, ResourceType received) {
        if ((tribe.getType() == TribeType.FARMER ||
                tribe.getType() == TribeType.COASTAL) && received != ResourceType.FOOD) {
            throw new IllegalArgumentException("This tribe only offers food");
        }
        if (tribe.getType() == TribeType.MOUNTAIN &&
                received != ResourceType.STONE && received != ResourceType.IRON) {
            throw new IllegalArgumentException("Mountain tribe offers stone or iron");
        }
    }

    private Tribe requireTribe(int tribeId) {
        return gameState.getTribeById(tribeId)
                .orElseThrow(() -> new IllegalArgumentException("Unknown tribe id"));
    }
}
