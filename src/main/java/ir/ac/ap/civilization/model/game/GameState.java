package ir.ac.ap.civilization.model.game;

import ir.ac.ap.civilization.model.building.TownHall;
import ir.ac.ap.civilization.model.building.BuildingType;
import ir.ac.ap.civilization.model.building.ResourceBuilding;
import ir.ac.ap.civilization.model.building.MaintainedBuilding;
import ir.ac.ap.civilization.model.building.Settlement;
import ir.ac.ap.civilization.model.building.SpecialBuilding;
import ir.ac.ap.civilization.model.building.SpecialBuildingType;
import ir.ac.ap.civilization.model.entity.EntityKey;
import ir.ac.ap.civilization.model.entity.EntityKind;
import ir.ac.ap.civilization.model.entity.GameEntity;
import ir.ac.ap.civilization.model.entity.Owner;
import ir.ac.ap.civilization.model.map.FogOfWarService;
import ir.ac.ap.civilization.model.map.GameMap;
import ir.ac.ap.civilization.model.map.HexCoordinate;
import ir.ac.ap.civilization.model.map.HexMapGenerator;
import ir.ac.ap.civilization.model.map.HexTile;
import ir.ac.ap.civilization.model.map.NaturalResourceType;
import ir.ac.ap.civilization.model.map.PlayerTerritory;
import ir.ac.ap.civilization.model.resource.ResourceStockpile;
import ir.ac.ap.civilization.model.resource.ResourceType;
import ir.ac.ap.civilization.model.random.DeterministicRandomSource;
import ir.ac.ap.civilization.model.random.RandomSource;
import ir.ac.ap.civilization.model.phase2.PhaseTwoState;
import ir.ac.ap.civilization.model.phase2.Difficulty;
import ir.ac.ap.civilization.model.tribe.Tribe;
import ir.ac.ap.civilization.model.tribe.TribeRelationState;
import ir.ac.ap.civilization.model.tribe.TribeType;
import ir.ac.ap.civilization.model.unit.Builder;
import ir.ac.ap.civilization.model.unit.Explorer;
import ir.ac.ap.civilization.model.unit.Unit;
import ir.ac.ap.civilization.model.unit.UnitFactory;
import ir.ac.ap.civilization.model.unit.UnitType;
import ir.ac.ap.civilization.model.unit.Worker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.io.Serializable;
import java.util.UUID;

public final class GameState implements Serializable {

    private static final long serialVersionUID = 2L;

    private static final int STARTING_FOOD = 60;
    private static final int STARTING_WOOD = 80;
    private static final int STARTING_STONE = 60;
    private static final int STARTING_IRON = 0;
    private static final int STARTING_TERRITORY_RADIUS = 2;
    private static final int BASE_UNIT_CAP = 8;

    private static final HexCoordinate TOWN_HALL_COORDINATE =
            new HexCoordinate(0, 0);

    private final TownHall townHall;
    private final String gameId;
    private final GameMap gameMap;
    private final PlayerTerritory playerTerritory;
    private final List<Unit> units;
    private final List<ResourceBuilding> resourceBuildings;
    private final List<Settlement> settlements;
    private final List<SpecialBuilding> specialBuildings;
    private final Map<String, Integer> lastTradeTurns;
    private final List<Tribe> tribes;
    private final PhaseTwoState phaseTwoState;
    private final Difficulty difficulty;
    private final FogOfWarService fogOfWarService;
    private final UnitFactory unitFactory;
    private final EconomyStatus economyStatus;
    private final DeterministicRandomSource randomSource;

    private int turnNumber;
    private int nextUnitId;
    private int nextBuildingId;
    private GameOperation currentOperation;

    public GameState() {
        this(HexMapGenerator.DEFAULT_SEED);
    }

    public GameState(long mapSeed) {
        this(new HexMapGenerator().generate(mapSeed), Difficulty.NORMAL);
    }

    public GameState(long mapSeed, Difficulty difficulty) {
        this(new HexMapGenerator().generate(mapSeed), difficulty);
    }

    GameState(GameMap gameMap) {
        this(gameMap, Difficulty.NORMAL);
    }

    public GameState(GameMap gameMap, Difficulty difficulty) {
        this.turnNumber = 1;
        this.nextUnitId = 1;
        this.nextBuildingId = 1;
        this.townHall = new TownHall();
        this.gameId = UUID.randomUUID().toString();
        this.gameMap = Objects.requireNonNull(
                gameMap,
                "Game map cannot be null"
        );
        this.playerTerritory = new PlayerTerritory(gameMap);
        this.units = new ArrayList<>();
        this.resourceBuildings = new ArrayList<>();
        this.settlements = new ArrayList<>();
        this.specialBuildings = new ArrayList<>();
        this.lastTradeTurns = new LinkedHashMap<>();
        this.tribes = new ArrayList<>();
        this.phaseTwoState = new PhaseTwoState();
        this.difficulty = Objects.requireNonNull(
                difficulty,
                "Difficulty cannot be null"
        );
        this.fogOfWarService = new FogOfWarService();
        this.unitFactory = new UnitFactory();
        this.economyStatus = new EconomyStatus();
        this.randomSource = new DeterministicRandomSource(
                gameMap.getSeed()
        );
        this.currentOperation = GameOperation.STABLE;

        initializeStartingResources();
        initializeStartingTerritory();
        initializeStartingUnits();
        initializeTribes();
        refreshVisibility();
    }

    public int getTurnNumber() {
        return turnNumber;
    }

    public String getGameId() {
        return gameId;
    }

    public Difficulty getDifficulty() {
        return difficulty == null ? Difficulty.NORMAL : difficulty;
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

    public PhaseTwoState getPhaseTwoState() {
        return phaseTwoState;
    }

    public List<Tribe> getTribes() {
        return Collections.unmodifiableList(tribes);
    }

    public Optional<Tribe> getTribeById(int tribeId) {
        for (Tribe tribe : tribes) {
            if (tribe.getId() == tribeId) return Optional.of(tribe);
        }
        return Optional.empty();
    }

    public boolean hasAlliedTribe(TribeType type) {
        for (Tribe tribe : tribes) {
            if (tribe.getType() == type &&
                    tribe.getRelationState() == TribeRelationState.ALLIED) {
                return true;
            }
        }
        return false;
    }

    public RandomSource getRandomSource() {
        return randomSource;
    }

    public GameOperation getCurrentOperation() {
        return currentOperation;
    }

    public boolean isStableForPersistence() {
        return currentOperation == GameOperation.STABLE;
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

    public List<SpecialBuilding> getSpecialBuildings() {
        return Collections.unmodifiableList(specialBuildings);
    }

    public boolean wasTradeUsedThisTurn(String sourceKey) {
        return Integer.valueOf(turnNumber).equals(
                lastTradeTurns.get(sourceKey)
        );
    }

    public void recordTrade(String sourceKey) {
        Objects.requireNonNull(sourceKey, "Trade source cannot be null");
        lastTradeTurns.put(sourceKey, turnNumber);
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

    public int getActiveMilitaryUnitCount() {
        int count = 0;
        for (Unit unit : units) {
            if (unit.getUnitType().isMilitary()) {
                count = Math.incrementExact(count);
            }
        }
        return count;
    }

    public int getMilitaryUnitCap() {
        switch (townHall.getCurrentLevel()) {
            case BASE_CAMP:
                return 5;
            case SETTLEMENT:
                return 10;
            case CAPITAL:
                return 15;
            default:
                throw new IllegalStateException("Unknown Town Hall level");
        }
    }

    public boolean canCreateUnit(UnitType unitType) {
        Objects.requireNonNull(unitType, "Unit type cannot be null");
        return canCreateUnit() &&
                (!unitType.isMilitary() ||
                        getActiveMilitaryUnitCount() < getMilitaryUnitCap());
    }

    public boolean hasBuildingType(BuildingType buildingType) {
        for (ResourceBuilding building : resourceBuildings) {
            if (building.getBuildingType() == buildingType) {
                return true;
            }
        }
        return false;
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

    public Optional<MaintainedBuilding> getBuildingById(
            int buildingId
    ) {
        Optional<ResourceBuilding> resourceBuilding =
                getResourceBuildingById(buildingId);

        if (resourceBuilding.isPresent()) {
            return Optional.<MaintainedBuilding>of(
                    resourceBuilding.get()
            );
        }

        for (Settlement settlement : settlements) {
            if (settlement.getId() == buildingId) {
                return Optional.<MaintainedBuilding>of(settlement);
            }
        }

        for (SpecialBuilding building : specialBuildings) {
            if (building.getId() == buildingId) {
                return Optional.<MaintainedBuilding>of(building);
            }
        }

        return Optional.empty();
    }

    public Optional<GameEntity> getEntity(EntityKey entityKey) {
        Objects.requireNonNull(
                entityKey,
                "Entity key cannot be null"
        );

        EntityKind kind = entityKey.getKind();

        if (kind == EntityKind.TOWN_HALL) {
            return townHall.getId() == entityKey.getId()
                    ? Optional.<GameEntity>of(townHall)
                    : Optional.<GameEntity>empty();
        }

        if (kind == EntityKind.UNIT) {
            Optional<Unit> unit = getUnitById(entityKey.getId());
            return unit.isPresent()
                    ? Optional.<GameEntity>of(unit.get())
                    : Optional.<GameEntity>empty();
        }

        if (kind == EntityKind.BUILDING) {
            Optional<MaintainedBuilding> building =
                    getBuildingById(entityKey.getId());
            return building.isPresent()
                    ? Optional.<GameEntity>of(building.get())
                    : Optional.<GameEntity>empty();
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

    public Optional<SpecialBuilding> getSpecialBuildingAt(
            HexCoordinate coordinate
    ) {
        Objects.requireNonNull(coordinate, "Coordinate cannot be null");
        for (SpecialBuilding building : specialBuildings) {
            if (building.getPosition().equals(coordinate)) {
                return Optional.of(building);
            }
        }
        return Optional.empty();
    }

    public SpecialBuilding addSpecialBuilding(
            SpecialBuildingType type,
            HexCoordinate position,
            Owner owner
    ) {
        Objects.requireNonNull(type, "Building type cannot be null");
        Objects.requireNonNull(position, "Position cannot be null");
        if (getResourceBuildingAt(position).isPresent() ||
                getSettlementAt(position).isPresent() ||
                getSpecialBuildingAt(position).isPresent()) {
            throw new IllegalStateException("A building already exists on this hex");
        }
        if (!gameMap.containsCoordinate(position)) {
            throw new IllegalArgumentException("Building is outside the map");
        }
        SpecialBuilding building = new SpecialBuilding(
                createNextBuildingId(), type, position, owner
        );
        specialBuildings.add(building);
        refreshVisibility();
        return building;
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
        List<Unit> playerUnits = new ArrayList<>();
        for (Unit unit : units) {
            if (unit.getOwner().isPlayer()) playerUnits.add(unit);
        }
        List<MaintainedBuilding> playerBuildings = new ArrayList<>();
        for (MaintainedBuilding building : getMaintainedBuildings()) {
            if (building.getOwner().isPlayer()) playerBuildings.add(building);
        }
        fogOfWarService.refreshVisibility(
                gameMap,
                TOWN_HALL_COORDINATE,
                playerUnits,
                playerBuildings
        );
        for (Tribe tribe : tribes) {
            HexTile campTile = gameMap.getTile(
                    tribe.getCampPosition()
            ).orElse(null);
            if (campTile != null && campTile.isDiscovered()) {
                tribe.discover();
            }
        }
    }

    void incrementTurnNumber() {
        turnNumber = Math.incrementExact(turnNumber);
    }

    void beginOperation(GameOperation operation) {
        Objects.requireNonNull(
                operation,
                "Game operation cannot be null"
        );
        if (operation == GameOperation.STABLE ||
                operation == GameOperation.ERROR) {
            throw new IllegalArgumentException(
                    "Cannot begin a terminal game operation"
            );
        }
        if (!isStableForPersistence()) {
            throw new IllegalStateException(
                    "Another game operation is already active"
            );
        }
        currentOperation = operation;
    }

    void completeOperation() {
        currentOperation = GameOperation.STABLE;
    }

    void failOperation() {
        currentOperation = GameOperation.ERROR;
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
                findDeploymentCoordinate(unitType)
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

    void addUnit(Unit unit) {
        Objects.requireNonNull(unit, "Unit cannot be null");
        if (getUnitById(unit.getId()).isPresent()) {
            throw new IllegalArgumentException("Duplicate unit id");
        }
        units.add(unit);
        nextUnitId = Math.max(nextUnitId, unit.getId() + 1);
        refreshVisibility();
    }

    int reserveUnitId() {
        return createNextUnitId();
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

        if (building instanceof SpecialBuilding) {
            if (!specialBuildings.remove(building)) {
                throw new IllegalStateException(
                        "Special building does not belong to this game"
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
        buildings.addAll(specialBuildings);

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

    private void initializeTribes() {
        TribeType[] types = {
                TribeType.FARMER,
                TribeType.MOUNTAIN,
                TribeType.MERCHANT,
                TribeType.COASTAL,
                TribeType.WARRIOR
        };
        int created = 0;
        for (HexTile tile : gameMap.getTiles()) {
            HexCoordinate coordinate = tile.getCoordinate();
            if (created >= types.length) break;
            if (TOWN_HALL_COORDINATE.distanceTo(coordinate) < 4 ||
                    !tile.isNormallyPassable()) {
                continue;
            }
            tribes.add(new Tribe(
                    created + 1,
                    types[created].name() + " Tribe",
                    types[created],
                    coordinate
            ));
            created++;
        }
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

    private HexCoordinate findDeploymentCoordinate(UnitType unitType) {
        if (unitType.isNaval()) {
            for (SpecialBuilding building : specialBuildings) {
                if (building.getType() != SpecialBuildingType.DOCK ||
                        !building.getOwner().isPlayer() ||
                        building.isDestroyed()) continue;
                for (HexTile neighbor : gameMap.getValidNeighbors(
                        building.getPosition())) {
                    if (neighbor.getTerrainType() ==
                            ir.ac.ap.civilization.model.map.TerrainType.SEA) {
                        return neighbor.getCoordinate();
                    }
                }
            }
            throw new IllegalStateException(
                    "A Dock with an adjacent sea hex is required"
            );
        }
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
