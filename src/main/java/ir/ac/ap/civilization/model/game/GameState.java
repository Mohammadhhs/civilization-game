package ir.ac.ap.civilization.model.game;

import ir.ac.ap.civilization.model.building.TownHall;
import ir.ac.ap.civilization.model.resource.ResourceStockpile;
import ir.ac.ap.civilization.model.resource.ResourceType;

public final class GameState {

    private static final int STARTING_FOOD = 60;
    private static final int STARTING_WOOD = 80;
    private static final int STARTING_STONE = 60;
    private static final int STARTING_IRON = 0;

    private int turnNumber;
    private final TownHall townHall;

    public GameState() {
        this.turnNumber = 1;
        this.townHall = new TownHall();

        initializeStartingResources();
    }

    public int getTurnNumber() {
        return turnNumber;
    }

    public TownHall getTownHall() {
        return townHall;
    }

    void incrementTurnNumber() {
        turnNumber = Math.incrementExact(turnNumber);
    }

    private void initializeStartingResources() {
        ResourceStockpile stockpile =
                townHall.getStockpile();

        stockpile.add(
                ResourceType.FOOD,
                STARTING_FOOD
        );

        stockpile.add(
                ResourceType.WOOD,
                STARTING_WOOD
        );

        stockpile.add(
                ResourceType.STONE,
                STARTING_STONE
        );

        stockpile.add(
                ResourceType.IRON,
                STARTING_IRON
        );
    }
}