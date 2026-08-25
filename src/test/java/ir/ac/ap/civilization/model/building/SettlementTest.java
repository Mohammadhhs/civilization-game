package ir.ac.ap.civilization.model.building;

import ir.ac.ap.civilization.model.map.HexCoordinate;
import ir.ac.ap.civilization.model.resource.ResourceType;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SettlementTest {

    @Test
    void settlementShouldExposeConstructionAndUpkeepRules() {
        Settlement settlement = new Settlement(
                1,
                new HexCoordinate(1, 0)
        );

        Map<ResourceType, Integer> constructionCost =
                Settlement.getConstructionCost();

        assertEquals(60, constructionCost.get(ResourceType.WOOD));
        assertEquals(50, constructionCost.get(ResourceType.STONE));
        assertEquals(20, constructionCost.get(ResourceType.IRON));
        assertEquals(3, Settlement.CONSTRUCTION_ACTION_POINT_COST);
        assertEquals(5, settlement.getUnitCapBonus());
        assertEquals(1, settlement.getUpkeepCost().get(ResourceType.WOOD));
        assertEquals(1, settlement.getUpkeepCost().get(ResourceType.STONE));
    }

    @Test
    void threeConsecutiveMissesShouldMarkSettlementForDestruction() {
        Settlement settlement = new Settlement(
                1,
                new HexCoordinate(1, 0)
        );

        assertFalse(settlement.isAtRiskFromUpkeep());

        settlement.recordMissedUpkeep();
        settlement.recordMissedUpkeep();

        assertTrue(settlement.isAtRiskFromUpkeep());
        assertFalse(settlement.shouldBeDestroyedForUpkeep());

        settlement.recordMissedUpkeep();

        assertTrue(settlement.shouldBeDestroyedForUpkeep());

        settlement.recordUpkeepPaid();

        assertEquals(0, settlement.getConsecutiveMissedUpkeepTurns());
        assertFalse(settlement.isAtRiskFromUpkeep());
    }
}
