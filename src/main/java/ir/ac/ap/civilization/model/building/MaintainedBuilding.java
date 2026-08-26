package ir.ac.ap.civilization.model.building;

import ir.ac.ap.civilization.model.map.HexCoordinate;
import ir.ac.ap.civilization.model.resource.ResourceType;
import ir.ac.ap.civilization.model.entity.Damageable;
import ir.ac.ap.civilization.model.entity.GameEntity;
import ir.ac.ap.civilization.model.entity.OwnedEntity;

import java.util.Map;

public interface MaintainedBuilding extends
        GameEntity, OwnedEntity, Damageable {

    int MAXIMUM_CONSECUTIVE_MISSED_UPKEEP_TURNS = 3;

    HexCoordinate getPosition();

    int getVisionRadius();

    Map<ResourceType, Integer> getUpkeepCost();

    int getConsecutiveMissedUpkeepTurns();

    boolean isAtRiskFromUpkeep();

    boolean shouldBeDestroyedForUpkeep();

    void recordUpkeepPaid();

    void recordMissedUpkeep();
}
