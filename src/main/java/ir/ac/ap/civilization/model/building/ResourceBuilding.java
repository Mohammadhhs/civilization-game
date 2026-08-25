package ir.ac.ap.civilization.model.building;

import ir.ac.ap.civilization.model.map.HexCoordinate;
import ir.ac.ap.civilization.model.map.NaturalResourceType;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

public final class ResourceBuilding {

    public static final int
            MAXIMUM_CONSECUTIVE_MISSED_UPKEEP_TURNS = 3;

    private final int id;
    private final BuildingType buildingType;
    private final HexCoordinate position;
    private final NaturalResourceType naturalResourceType;
    private final LinkedHashSet<Integer> stationedWorkerIds;

    private int consecutiveMissedUpkeepTurns;

    public ResourceBuilding(
            int id,
            BuildingType buildingType,
            HexCoordinate position,
            NaturalResourceType naturalResourceType
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

    public int getId() {
        return id;
    }

    public BuildingType getBuildingType() {
        return buildingType;
    }

    public HexCoordinate getPosition() {
        return position;
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

    public int getConsecutiveMissedUpkeepTurns() {
        return consecutiveMissedUpkeepTurns;
    }

    public boolean isAtRiskFromUpkeep() {
        return consecutiveMissedUpkeepTurns > 0;
    }

    public boolean shouldBeDestroyedForUpkeep() {
        return consecutiveMissedUpkeepTurns >=
                MAXIMUM_CONSECUTIVE_MISSED_UPKEEP_TURNS;
    }

    public void recordUpkeepPaid() {
        consecutiveMissedUpkeepTurns = 0;
    }

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
