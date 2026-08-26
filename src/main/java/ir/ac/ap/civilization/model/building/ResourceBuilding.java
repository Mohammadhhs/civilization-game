package ir.ac.ap.civilization.model.building;

import ir.ac.ap.civilization.model.entity.EntityKey;
import ir.ac.ap.civilization.model.entity.EntityKind;
import ir.ac.ap.civilization.model.entity.HitPoints;
import ir.ac.ap.civilization.model.entity.Owner;
import ir.ac.ap.civilization.model.map.HexCoordinate;
import ir.ac.ap.civilization.model.map.NaturalResourceType;
import ir.ac.ap.civilization.model.resource.ResourceType;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class ResourceBuilding
        implements MaintainedBuilding {

    private static final int VISION_RADIUS = 1;
    private static final int MAXIMUM_HIT_POINTS = 100;

    public static final int
            MAXIMUM_CONSECUTIVE_MISSED_UPKEEP_TURNS =
            MaintainedBuilding
                    .MAXIMUM_CONSECUTIVE_MISSED_UPKEEP_TURNS;

    private final int id;
    private final BuildingType buildingType;
    private final HexCoordinate position;
    private final NaturalResourceType naturalResourceType;
    private final LinkedHashSet<Integer> stationedWorkerIds;
    private final Owner owner;
    private final HitPoints hitPoints;

    private int consecutiveMissedUpkeepTurns;

    public ResourceBuilding(
            int id,
            BuildingType buildingType,
            HexCoordinate position,
            NaturalResourceType naturalResourceType
    ) {
        this(id, buildingType, position, naturalResourceType,
                Owner.PLAYER, MAXIMUM_HIT_POINTS,
                MAXIMUM_HIT_POINTS);
    }

    public ResourceBuilding(
            int id,
            BuildingType buildingType,
            HexCoordinate position,
            NaturalResourceType naturalResourceType,
            Owner owner,
            int currentHitPoints,
            int maximumHitPoints
    ) {
        if (id <= 0) {
            throw new IllegalArgumentException(
                    "Building id must be positive"
            );
        }

        this.id = id;
        this.buildingType = Objects.requireNonNull(
                buildingType,
                "Building type cannot be null"
        );
        this.position = Objects.requireNonNull(
                position,
                "Building position cannot be null"
        );
        this.naturalResourceType = Objects.requireNonNull(
                naturalResourceType,
                "Natural resource type cannot be null"
        );
        this.owner = Objects.requireNonNull(
                owner,
                "Building owner cannot be null"
        );
        this.hitPoints = new HitPoints(
                currentHitPoints,
                maximumHitPoints
        );

        if (!buildingType.supportsNaturalResource(
                naturalResourceType
        )) {
            throw new IllegalArgumentException(
                    "Building is incompatible with natural resource"
            );
        }

        stationedWorkerIds = new LinkedHashSet<>();
        consecutiveMissedUpkeepTurns = 0;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public EntityKey getEntityKey() {
        return new EntityKey(EntityKind.BUILDING, id);
    }

    @Override
    public Owner getOwner() {
        return owner;
    }

    @Override
    public int getCurrentHitPoints() {
        return hitPoints.getCurrent();
    }

    @Override
    public int getMaximumHitPoints() {
        return hitPoints.getMaximum();
    }

    @Override
    public void takeDamage(int amount) {
        hitPoints.damage(amount);
    }

    @Override
    public void heal(int amount) {
        hitPoints.heal(amount);
    }

    public BuildingType getBuildingType() {
        return buildingType;
    }

    @Override
    public HexCoordinate getPosition() {
        return position;
    }

    @Override
    public int getVisionRadius() {
        return VISION_RADIUS;
    }

    public NaturalResourceType getNaturalResourceType() {
        return naturalResourceType;
    }

    public Set<Integer> getStationedWorkerIds() {
        return Collections.unmodifiableSet(
                new LinkedHashSet<>(stationedWorkerIds)
        );
    }

    public int getStationedWorkerCount() {
        return stationedWorkerIds.size();
    }

    public int getWorkerCapacity() {
        return buildingType.getWorkerCapacity();
    }

    public boolean hasWorkerCapacity() {
        return getStationedWorkerCount() < getWorkerCapacity();
    }

    public boolean containsWorker(int workerId) {
        return stationedWorkerIds.contains(workerId);
    }

    @Override
    public Map<ResourceType, Integer> getUpkeepCost() {
        return buildingType.getUpkeepCost();
    }

    @Override
    public int getConsecutiveMissedUpkeepTurns() {
        return consecutiveMissedUpkeepTurns;
    }

    @Override
    public boolean isAtRiskFromUpkeep() {
        return consecutiveMissedUpkeepTurns > 0;
    }

    @Override
    public boolean shouldBeDestroyedForUpkeep() {
        return consecutiveMissedUpkeepTurns >=
                MAXIMUM_CONSECUTIVE_MISSED_UPKEEP_TURNS;
    }

    @Override
    public void recordUpkeepPaid() {
        consecutiveMissedUpkeepTurns = 0;
    }

    @Override
    public void recordMissedUpkeep() {
        consecutiveMissedUpkeepTurns = Math.incrementExact(
                consecutiveMissedUpkeepTurns
        );
    }

    public void stationWorker(int workerId) {
        if (workerId <= 0) {
            throw new IllegalArgumentException(
                    "Worker id must be positive"
            );
        }

        if (containsWorker(workerId)) {
            throw new IllegalStateException(
                    "Worker is already stationed in this building"
            );
        }

        if (!hasWorkerCapacity()) {
            throw new IllegalStateException(
                    "Building worker capacity is full"
            );
        }

        stationedWorkerIds.add(workerId);
    }

    public void removeWorker(int workerId) {
        if (!stationedWorkerIds.remove(workerId)) {
            throw new IllegalStateException(
                    "Worker is not stationed in this building"
            );
        }
    }

    public void removeAllWorkers() {
        stationedWorkerIds.clear();
    }
}
