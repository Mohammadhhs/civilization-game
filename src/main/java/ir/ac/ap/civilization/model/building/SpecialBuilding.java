package ir.ac.ap.civilization.model.building;

import ir.ac.ap.civilization.model.entity.EntityKey;
import ir.ac.ap.civilization.model.entity.EntityKind;
import ir.ac.ap.civilization.model.entity.HitPoints;
import ir.ac.ap.civilization.model.entity.Owner;
import ir.ac.ap.civilization.model.map.HexCoordinate;
import ir.ac.ap.civilization.model.resource.ResourceType;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

public final class SpecialBuilding implements MaintainedBuilding {

    private final int id;
    private final SpecialBuildingType type;
    private final HexCoordinate position;
    private final Owner owner;
    private final HitPoints hitPoints;
    private int missedUpkeep;

    public SpecialBuilding(
            int id,
            SpecialBuildingType type,
            HexCoordinate position,
            Owner owner
    ) {
        if (id <= 0) throw new IllegalArgumentException("Building id must be positive");
        this.id = id;
        this.type = Objects.requireNonNull(type, "Type cannot be null");
        this.position = Objects.requireNonNull(position, "Position cannot be null");
        this.owner = Objects.requireNonNull(owner, "Owner cannot be null");
        this.hitPoints = new HitPoints(type.getMaximumHitPoints());
    }

    @Override public int getId() { return id; }
    @Override public EntityKey getEntityKey() {
        return new EntityKey(EntityKind.BUILDING, id);
    }
    public SpecialBuildingType getType() { return type; }
    @Override public HexCoordinate getPosition() { return position; }
    @Override public Owner getOwner() { return owner; }
    @Override public int getVisionRadius() { return 2; }
    @Override public Map<ResourceType, Integer> getUpkeepCost() {
        return Collections.singletonMap(ResourceType.WOOD, 1);
    }
    @Override public int getConsecutiveMissedUpkeepTurns() { return missedUpkeep; }
    @Override public boolean isAtRiskFromUpkeep() { return missedUpkeep > 0; }
    @Override public boolean shouldBeDestroyedForUpkeep() {
        return missedUpkeep >= MAXIMUM_CONSECUTIVE_MISSED_UPKEEP_TURNS;
    }
    @Override public void recordUpkeepPaid() { missedUpkeep = 0; }
    @Override public void recordMissedUpkeep() { missedUpkeep++; }
    @Override public int getCurrentHitPoints() { return hitPoints.getCurrent(); }
    @Override public int getMaximumHitPoints() { return hitPoints.getMaximum(); }
    @Override public void takeDamage(int amount) { hitPoints.damage(amount); }
    @Override public void heal(int amount) { hitPoints.heal(amount); }
}
