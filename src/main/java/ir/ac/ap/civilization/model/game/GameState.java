package ir.ac.ap.civilization.model.game;

import ir.ac.ap.civilization.model.building.TownHall;
import ir.ac.ap.civilization.model.map.GameMap;
import ir.ac.ap.civilization.model.map.HexCoordinate;
import ir.ac.ap.civilization.model.map.HexMapGenerator;
import ir.ac.ap.civilization.model.resource.ResourceStockpile;
import ir.ac.ap.civilization.model.resource.ResourceType;

public final class GameState {

    private static final int STARTING_FOOD = 60;
    private static final int STARTING_WOOD = 80;
    private static final int STARTING_STONE = 60;
    private static final int STARTING_IRON = 0;

    private static final HexCoordinate TOWN_HALL_COORDINATE =
            new HexCoordinate(0, 0);

    private int turnNumber;
    private final TownHall townHall;
    private final GameMap gameMap;

    public GameState() {
        this(HexMapGenerator.DEFAULT_SEED);
    }

    public GameState(long mapSeed) {
        this.turnNumber = 1;
        this.townHall = new TownHall();
        this.gameMap = new HexMapGenerator().generate(mapSeed);

        initializeStartingResources();
    }

    public int getTurnNumber() {
        return turnNumber;
    }

    public TownHall getTownHall() {
        return townHall;
    }

    public GameMap getGameMap() {
        return gameMap;
    }

    public HexCoordinate getTownHallCoordinate() {
        return TOWN_HALL_COORDINATE;
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