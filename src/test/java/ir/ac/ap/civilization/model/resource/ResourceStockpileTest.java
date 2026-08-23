package ir.ac.ap.civilization.model.resource;

import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResourceStockpileTest {

    @Test
    void newStockpileShouldInitializeAllResourcesToZero() {
        ResourceStockpile stockpile = new ResourceStockpile(100);

        for (ResourceType resourceType : ResourceType.values()) {
            assertEquals(0, stockpile.getAmount(resourceType));
        }

        assertEquals(100, stockpile.getCapacityPerResource());
    }

    @Test
    void addShouldIncreaseResourceAmount() {
        ResourceStockpile stockpile = new ResourceStockpile(100);

        stockpile.add(ResourceType.WOOD, 30);
        stockpile.add(ResourceType.WOOD, 20);

        assertEquals(50, stockpile.getAmount(ResourceType.WOOD));
    }

    @Test
    void overflowShouldRemainUntilCapacityLimitIsApplied() {
        ResourceStockpile stockpile = new ResourceStockpile(100);

        stockpile.add(ResourceType.FOOD, 130);

        assertEquals(130, stockpile.getAmount(ResourceType.FOOD));
        assertTrue(stockpile.isOverCapacity(ResourceType.FOOD));

        stockpile.applyCapacityLimit();

        assertEquals(100, stockpile.getAmount(ResourceType.FOOD));
        assertFalse(stockpile.isOverCapacity(ResourceType.FOOD));
    }

    @Test
    void spendShouldDecreaseResourceAmount() {
        ResourceStockpile stockpile = new ResourceStockpile(100);

        stockpile.add(ResourceType.STONE, 70);
        stockpile.spend(ResourceType.STONE, 25);

        assertEquals(45, stockpile.getAmount(ResourceType.STONE));
    }

    @Test
    void spendingMoreThanAvailableShouldFailWithoutChangingAmount() {
        ResourceStockpile stockpile = new ResourceStockpile(100);
        stockpile.add(ResourceType.IRON, 10);

        assertThrows(
                IllegalStateException.class,
                () -> stockpile.spend(ResourceType.IRON, 15)
        );

        assertEquals(10, stockpile.getAmount(ResourceType.IRON));
    }

    @Test
    void canAffordShouldCheckMultipleResources() {
        ResourceStockpile stockpile = new ResourceStockpile(100);

        stockpile.add(ResourceType.WOOD, 50);
        stockpile.add(ResourceType.STONE, 30);

        Map<ResourceType, Integer> affordableCosts =
                new EnumMap<>(ResourceType.class);

        affordableCosts.put(ResourceType.WOOD, 40);
        affordableCosts.put(ResourceType.STONE, 20);

        assertTrue(stockpile.canAfford(affordableCosts));

        affordableCosts.put(ResourceType.STONE, 40);

        assertFalse(stockpile.canAfford(affordableCosts));
    }

    @Test
    void spendingMultipleResourcesShouldBeAtomic() {
        ResourceStockpile stockpile = new ResourceStockpile(100);

        stockpile.add(ResourceType.WOOD, 50);
        stockpile.add(ResourceType.STONE, 10);

        Map<ResourceType, Integer> costs =
                new EnumMap<>(ResourceType.class);

        costs.put(ResourceType.WOOD, 20);
        costs.put(ResourceType.STONE, 15);

        assertThrows(
                IllegalStateException.class,
                () -> stockpile.spend(costs)
        );

        assertEquals(50, stockpile.getAmount(ResourceType.WOOD));
        assertEquals(10, stockpile.getAmount(ResourceType.STONE));
    }

    @Test
    void spendingMultipleResourcesShouldDecreaseAllAmounts() {
        ResourceStockpile stockpile = new ResourceStockpile(100);

        stockpile.add(ResourceType.WOOD, 60);
        stockpile.add(ResourceType.STONE, 40);
        stockpile.add(ResourceType.IRON, 20);

        Map<ResourceType, Integer> costs =
                new EnumMap<>(ResourceType.class);

        costs.put(ResourceType.WOOD, 20);
        costs.put(ResourceType.STONE, 15);
        costs.put(ResourceType.IRON, 5);

        stockpile.spend(costs);

        assertEquals(40, stockpile.getAmount(ResourceType.WOOD));
        assertEquals(25, stockpile.getAmount(ResourceType.STONE));
        assertEquals(15, stockpile.getAmount(ResourceType.IRON));
    }

    @Test
    void reducingCapacityShouldNotImmediatelyRemoveResources() {
        ResourceStockpile stockpile = new ResourceStockpile(100);

        stockpile.add(ResourceType.FOOD, 80);
        stockpile.setCapacityPerResource(50);

        assertEquals(80, stockpile.getAmount(ResourceType.FOOD));

        stockpile.applyCapacityLimit();

        assertEquals(50, stockpile.getAmount(ResourceType.FOOD));
    }

    @Test
    void negativeValuesShouldBeRejected() {
        ResourceStockpile stockpile = new ResourceStockpile(100);

        assertThrows(
                IllegalArgumentException.class,
                () -> stockpile.add(ResourceType.FOOD, -1)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> stockpile.spend(ResourceType.FOOD, -1)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> stockpile.setCapacityPerResource(-1)
        );
    }

    @Test
    void returnedAmountsShouldBeUnmodifiable() {
        ResourceStockpile stockpile = new ResourceStockpile(100);
        Map<ResourceType, Integer> amounts = stockpile.getAmounts();

        assertThrows(
                UnsupportedOperationException.class,
                () -> amounts.put(ResourceType.FOOD, 500)
        );

        assertEquals(0, stockpile.getAmount(ResourceType.FOOD));
    }
}