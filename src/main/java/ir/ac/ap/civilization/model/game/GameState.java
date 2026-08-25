package ir.ac.ap.civilization.model.game;

import ir.ac.ap.civilization.model.building.TownHall;
import ir.ac.ap.civilization.model.building.BuildingType;
import ir.ac.ap.civilization.model.building.ResourceBuilding;
import ir.ac.ap.civilization.model.building.MaintainedBuilding;
import ir.ac.ap.civilization.model.building.Settlement;
import ir.ac.ap.civilization.model.map.FogOfWarService;
import ir.ac.ap.civilization.model.map.GameMap;
import ir.ac.ap.civilization.model.map.HexCoordinate;
import ir.ac.ap.civilization.model.map.HexMapGenerator;
import ir.ac.ap.civilization.model.map.HexTile;
import ir.ac.ap.civilization.model.map.NaturalResourceType;
import ir.ac.ap.civilization.model.map.PlayerTerritory;
import ir.ac.ap.civilization.model.resource.ResourceStockpile;
import ir.ac.ap.civilization.model.resource.ResourceType;
import ir.ac.ap.civilization.model.unit.Builder;
import ir.ac.ap.civilization.model.unit.Explorer;
import ir.ac.ap.civilization.model.unit.Unit;
import ir.ac.ap.civilization.model.unit.UnitFactory;
import ir.ac.ap.civilization.model.unit.UnitType;
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
    private static final int BASE_UNIT_CAP = 8;

    private static final HexCoordinate TOWN_HALL_COORDINATE =
            new HexCoordinate(0, 0);

    private final TownHall townHall;
    private final GameMap gameMap;
    private final PlayerTerritory playerTerritory;
    private final List<Unit> units;
    private final List<ResourceBuilding> resourceBuildings;
    private final List<Settlement> settlements;
    private final FogOfWarService fogOfWarService;
    private final UnitFactory unitFactory;
    private final EconomyStatus economyStatus;

    private int turnNumber;
    private int nextUnitId;
    private int nextBuildingId;

    public GameState() {
        this(HexMapGenerator.DEFAULT_SEED);
    }

    public GameState(long mapSeed) {
        this(new HexMapGenerator().generate(mapSeed));
    }

    GameState(GameMap gameMap) {
        this.turnNumber = 1;
        this.nextUnitId = 1;
        this.nextBuildingId = 1;
        this.townHall = new TownHall();
        this.gameMap = Objects.requireNonNull(
                gameMap,
                "Game map cannot be null"
        );
        this.playerTerritory = new PlayerTerritory(gameMap);
        this.units = new ArrayList<>();
        this.resourceBuildings = new ArrayList<>();
        this.settlements = new ArrayList<>();
        this.fogOfWarService = new FogOfWarService();
        this.unitFactory = new UnitFactory();
        this.economyStatus = new EconomyStatus();

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

    public EconomyStatus getEconomyStatus() {
        return economyStatus;
    }

    public List<Unit> getUnits() {
        return Collections.unmodifiableList(units);
    }

    public List<ResourceBuilding> getResourceBuildings() {
        return Collections.unmodifiableList(resourceBuildings);
    }

    public List<Settlement> getSettlements() {
        return Collections.unmodifiableList(settlements);
    }

    public int getActiveUnitCount() {
        return units.size();
    }

    public int getActiveUnitCount(UnitType unitType) {
        Objects.requireNonNull(
                unitType,
                "Unit type cannot be null"
        );

        int count = 0;

        for (Unit unit : units) {
            if (unit.getUnitType() == unitType) {
                count = Math.incrementExact(count);
            }
        }

        return count;
    }

    public List<Unit> getIdleUnits() {
        List<Unit> idleUnits = new ArrayList<>();

        for (Unit unit : units) {
            if (!unit.hasActionPoints()) {
                continue;
            }

            if (unit instanceof Worker &&
                    ((Worker) unit).isStationed()) {
                continue;
            }

            idleUnits.add(unit);
        }

        return Collections.unmodifiableList(idleUnits);
    }

    public int getUnitCap() {
        int unitCap = BASE_UNIT_CAP;

        for (Settlement settlement : settlements) {
            unitCap = Math.addExact(
                    unitCap,
                    settlement.getUnitCapBonus()
            );
        }

        return unitCap;
    }

    public int getAvailableUnitSlots() {
        return Math.max(
                0,
                getUnitCap() - getActiveUnitCount()
        );
    }

    public boolean canCreateUnit() {
        return getActiveUnitCount() < getUnitCap();
    }

    public boolean isOverUnitCap() {
        return getActiveUnitCount() > getUnitCap();
    }

    public Optional<ResourceBuilding> getResourceBuildingById(
            int buildingId
    ) {
        for (ResourceBuilding building : resourceBuildings) {
            if (building.getId() == buildingId) {
                return Optional.of(building);
            }
        }

        return Optional.empty();
    }

    public Optional<ResourceBuilding> getResourceBuildingAt(
            HexCoordinate coordinate
    ) {
        Objects.requireNonNull(
                coordinate,
                "Coordinate cannot be null"
        );

        for (ResourceBuilding building : resourceBuildings) {
            if (building.getPosition().equals(coordinate)) {
                return Optional.of(building);
            }
        }

        return Optional.empty();
    }

    public Optional<Settlement> getSettlementAt(
            HexCoordinate coordinate
    ) {
        Objects.requireNonNull(
                coordinate,
                "Coordinate cannot be null"
        );

        for (Settlement settlement : settlements) {
            if (settlement.getPosition().equals(coordinate)) {
                return Optional.of(settlement);
            }
        }

        return Optional.empty();
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

        return Collections.unmodifiableList(matchingUnits);
    }

    public void refreshVisibility() {
        fogOfWarService.refreshVisibility(
                gameMap,
                TOWN_HALL_COORDINATE,
                units,
                getMaintainedBuildings()
        );
    }

    void incrementTurnNumber() {
        turnNumber = Math.incrementExact(turnNumber);
    }

    void restoreAllUnitActionPoints() {
        for (Unit unit : units) {
            unit.restoreActionPoints();
        }
    }

    Unit deployUnit(UnitType unitType) {
        Objects.requireNonNull(
                unitType,
                "Unit type cannot be null"
        );

        Unit unit = unitFactory.create(
                createNextUnitId(),
                unitType,
                findDeploymentCoordinate()
        );

        units.add(unit);
        refreshVisibility();
        return unit;
    }

    ResourceBuilding addResourceBuilding(
            BuildingType buildingType,
            HexCoordinate position,
            NaturalResourceType naturalResourceType
    ) {
        if (getResourceBuildingAt(position).isPresent() ||
                getSettlementAt(position).isPresent()) {
            throw new IllegalStateException(
                    "A resource building already exists on this hex"
            );
        }

        ResourceBuilding building = new ResourceBuilding(
                createNextBuildingId(),
                buildingType,
                position,
                naturalResourceType
        );

        resourceBuildings.add(building);
        return building;
    }

    Settlement addSettlement(HexCoordinate position) {
        Objects.requireNonNull(
                position,
                "Settlement position cannot be null"
        );

        if (getResourceBuildingAt(position).isPresent() ||
                getSettlementAt(position).isPresent()) {
            throw new IllegalStateException(
                    "A building already exists on this hex"
            );
        }

        Settlement settlement = new Settlement(
                createNextBuildingId(),
                position
        );

        settlements.add(settlement);
        return settlement;
    }

    void removeUnit(Unit unit) {
        if (!units.remove(Objects.requireNonNull(
                unit,
                "Unit cannot be null"
        ))) {
            throw new IllegalStateException(
                    "Unit does not belong to this game"
            );
        }
    }

    void removeResourceBuilding(
            ResourceBuilding building
    ) {
        Objects.requireNonNull(
                building,
                "Resource building cannot be null"
        );

        if (!resourceBuildings.contains(building)) {
            throw new IllegalStateException(
                    "Building does not belong to this game"
            );
        }

        for (Integer workerId : new ArrayList<>(
                building.getStationedWorkerIds()
        )) {
            Unit unit = getUnitById(workerId).orElse(null);

            if (unit instanceof Worker) {
                Worker worker = (Worker) unit;

                if (worker.isStationed() &&
                        Integer.valueOf(building.getId()).equals(
                                worker.getStationedBuildingId()
                        )) {
                    worker.leaveBuilding();
                }
            }
        }

        building.removeAllWorkers();
        resourceBuildings.remove(building);
    }

    void removeMaintainedBuilding(
            MaintainedBuilding building
    ) {
        Objects.requireNonNull(
                building,
                "Building cannot be null"
        );

        if (building instanceof ResourceBuilding) {
            removeResourceBuilding(
                    (ResourceBuilding) building
            );
            return;
        }

        if (building instanceof Settlement) {
            if (!settlements.remove(building)) {
                throw new IllegalStateException(
                        "Settlement does not belong to this game"
                );
            }
            return;
        }

        throw new IllegalArgumentException(
                "Unknown maintained building type"
        );
    }

    List<MaintainedBuilding> getMaintainedBuildings() {
        List<MaintainedBuilding> buildings =
                new ArrayList<>();

        buildings.addAll(resourceBuildings);
        buildings.addAll(settlements);

        Collections.sort(
                buildings,
                (first, second) -> Integer.compare(
                        first.getId(),
                        second.getId()
                )
        );

        return Collections.unmodifiableList(buildings);
    }

    private void initializeStartingResources() {
        ResourceStockpile stockpile =
                townHall.getStockpile();

        stockpile.add(ResourceType.FOOD, STARTING_FOOD);
        stockpile.add(ResourceType.WOOD, STARTING_WOOD);
        stockpile.add(ResourceType.STONE, STARTING_STONE);
        stockpile.add(ResourceType.IRON, STARTING_IRON);
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
                    "Starting unit must be placed on a passable tile"
            );
        }

        if (!getUnitsAt(unit.getPosition()).isEmpty()) {
            throw new IllegalStateException(
                    "Starting units cannot share a tile"
            );
        }

        units.add(unit);
    }

    private int createNextUnitId() {
        int createdId = nextUnitId;
        nextUnitId = Math.incrementExact(nextUnitId);
        return createdId;
    }

    private int createNextBuildingId() {
        int createdId = nextBuildingId;
        nextBuildingId = Math.incrementExact(nextBuildingId);
        return createdId;
    }

    private HexCoordinate findDeploymentCoordinate() {
        List<HexCoordinate> candidates = new ArrayList<>();
        candidates.add(TOWN_HALL_COORDINATE);
        candidates.addAll(TOWN_HALL_COORDINATE.getNeighbors());

        for (HexCoordinate coordinate : candidates) {
            HexTile tile = gameMap.getTile(coordinate)
                    .orElse(null);

            if (tile != null &&
                    tile.isNormallyPassable() &&
                    getUnitsAt(coordinate).isEmpty()) {
                return coordinate;
            }
        }

        return TOWN_HALL_COORDINATE;
    }
}
