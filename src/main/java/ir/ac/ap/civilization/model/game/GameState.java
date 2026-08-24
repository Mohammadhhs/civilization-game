package ir.ac.ap.civilization.model.game;

import ir.ac.ap.civilization.model.building.TownHall;
import ir.ac.ap.civilization.model.map.FogOfWarService;
import ir.ac.ap.civilization.model.map.GameMap;
import ir.ac.ap.civilization.model.map.HexCoordinate;
import ir.ac.ap.civilization.model.map.HexMapGenerator;
import ir.ac.ap.civilization.model.map.HexTile;
import ir.ac.ap.civilization.model.map.PlayerTerritory;
import ir.ac.ap.civilization.model.resource.ResourceStockpile;
import ir.ac.ap.civilization.model.resource.ResourceType;
import ir.ac.ap.civilization.model.unit.Builder;
import ir.ac.ap.civilization.model.unit.Explorer;
import ir.ac.ap.civilization.model.unit.Unit;
import ir.ac.ap.civilization.model.unit.Worker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class GameState {

    private static final int STARTING_FOOD = 60;
    private static final int STARTING_WOOD = 80;
    private static final int STARTING_STONE = 60;
    private static final int STARTING_IRON = 0;

    private static final int STARTING_TERRITORY_RADIUS = 2;

    private static final HexCoordinate TOWN_HALL_COORDINATE =
            new HexCoordinate(0, 0);

    private final TownHall townHall;
    private final GameMap gameMap;
    private final PlayerTerritory playerTerritory;
    private final List<Unit> units;
    private final FogOfWarService fogOfWarService;

    private int turnNumber;
    private int nextUnitId;

    public GameState() {
        this(HexMapGenerator.DEFAULT_SEED);
    }

    public GameState(long mapSeed) {
        this.turnNumber = 1;
        this.nextUnitId = 1;
        this.townHall = new TownHall();

        this.gameMap =
                new HexMapGenerator().generate(mapSeed);

        this.playerTerritory =
                new PlayerTerritory(gameMap);

        this.units = new ArrayList<>();
        this.fogOfWarService = new FogOfWarService();

        initializeStartingResources();
        initializeStartingTerritory();
        initializeStartingUnits();
        refreshVisibility();
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

    public PlayerTerritory getPlayerTerritory() {
        return playerTerritory;
    }

    public List<Unit> getUnits() {
        return Collections.unmodifiableList(units);
    }

    public Optional<Unit> getUnitById(int unitId) {
        for (Unit unit : units) {
            if (unit.getId() == unitId) {
                return Optional.of(unit);
            }
        }

        return Optional.empty();
    }

    public List<Unit> getUnitsAt(
            HexCoordinate coordinate
    ) {
        Objects.requireNonNull(
                coordinate,
                "Coordinate cannot be null"
        );

        List<Unit> matchingUnits = new ArrayList<>();

        for (Unit unit : units) {
            if (unit.getPosition().equals(coordinate)) {
                matchingUnits.add(unit);
            }
        }

        return Collections.unmodifiableList(
                matchingUnits
        );
    }

    public void refreshVisibility() {
        fogOfWarService.refreshVisibility(
                gameMap,
                TOWN_HALL_COORDINATE,
                units
        );
    }

    void incrementTurnNumber() {
        turnNumber =
                Math.incrementExact(turnNumber);
    }

    void restoreAllUnitActionPoints() {
        for (Unit unit : units) {
            unit.restoreActionPoints();
        }
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

    private void initializeStartingTerritory() {
        playerTerritory.claimArea(
                TOWN_HALL_COORDINATE,
                STARTING_TERRITORY_RADIUS
        );
    }

    private void initializeStartingUnits() {
        List<HexCoordinate> startingPositions =
                TOWN_HALL_COORDINATE.getNeighbors();

        addStartingUnit(
                new Builder(
                        createNextUnitId(),
                        startingPositions.get(0)
                )
        );

        addStartingUnit(
                new Builder(
                        createNextUnitId(),
                        startingPositions.get(1)
                )
        );

        addStartingUnit(
                new Worker(
                        createNextUnitId(),
                        startingPositions.get(2)
                )
        );

        addStartingUnit(
                new Worker(
                        createNextUnitId(),
                        startingPositions.get(3)
                )
        );

        addStartingUnit(
                new Explorer(
                        createNextUnitId(),
                        startingPositions.get(4)
                )
        );
    }

    private void addStartingUnit(Unit unit) {
        HexTile startingTile = gameMap
                .getTile(unit.getPosition())
                .orElseThrow(
                        () -> new IllegalStateException(
                                "Starting unit is outside the map"
                        )
                );

        if (!startingTile.isNormallyPassable()) {
            throw new IllegalStateException(
                    "Starting unit must be placed " +
                            "on a passable tile"
            );
        }

        if (!getUnitsAt(
                unit.getPosition()
        ).isEmpty()) {

            throw new IllegalStateException(
                    "Starting units cannot share a tile"
            );
        }

        units.add(unit);
    }

    private int createNextUnitId() {
        int createdId = nextUnitId;

        nextUnitId =
                Math.incrementExact(nextUnitId);

        return createdId;
    }
}