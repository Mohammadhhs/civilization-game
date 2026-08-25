package ir.ac.ap.civilization.model.building;

import ir.ac.ap.civilization.model.map.HexCoordinate;
import ir.ac.ap.civilization.model.resource.ResourceType;

import java.util.Map;

public interface MaintainedBuilding {

    int MAXIMUM_CONSECUTIVE_MISSED_UPKEEP_TURNS = 3;

    int getId();

    HexCoordinate getPosition();

    Map<ResourceType, Integer> getUpkeepCost();

    int getConsecutiveMissedUpkeepTurns();

    boolean isAtRiskFromUpkeep();

    boolean shouldBeDestroyedForUpkeep();

    void recordUpkeepPaid();

    void recordMissedUpkeep();
}
