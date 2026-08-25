package ir.ac.ap.civilization.model.game;

import ir.ac.ap.civilization.model.building.BuildingType;
import ir.ac.ap.civilization.model.building.ResourceBuilding;
import ir.ac.ap.civilization.model.map.GameMap;
import ir.ac.ap.civilization.model.map.HexCoordinate;
import ir.ac.ap.civilization.model.map.HexTile;
import ir.ac.ap.civilization.model.map.NaturalResourceType;
import ir.ac.ap.civilization.model.map.TerrainType;
import ir.ac.ap.civilization.model.resource.ResourceType;
import ir.ac.ap.civilization.model.unit.Worker;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EconomyForecastServiceTest {

    private static final HexCoordinate CENTER =
            new HexCoordinate(0, 0);
    private static final HexCoordinate BUILDING_POSITION =
            new HexCoordinate(1, 0);

    @Test
    void initialForecastShouldIncludeProductionAndUnitFood() {
        GameState gameState = new GameState();

        ResourceFlowForecast forecast =
                new EconomyForecastService(gameState)
                        .calculate();

        assertEquals(-6, forecast.getNetChange(ResourceType.FOOD));
        assertEquals(4, forecast.getNetChange(ResourceType.WOOD));
        assertEquals(0, forecast.getNetChange(ResourceType.STONE));
        assertEquals(0, forecast.getNetChange(ResourceType.IRON));
    }

    @Test
    void activeBuildingProductionAndUpkeepShouldBeIncluded() {
        GameState gameState = createForestGameState();

        ResourceBuilding building =
                gameState.addResourceBuilding(
                        BuildingType.LUMBER_MILL,
                        BUILDING_POSITION,
                        NaturalResourceType.WOOD
                );

        Worker worker = (Worker) gameState.getUnitById(3)
                .orElseThrow(AssertionError::new);

        building.stationWorker(worker.getId());
        worker.stationAt(building.getId());

        ResourceFlowForecast forecast =
                new EconomyForecastService(gameState)
                        .calculate();

        assertEquals(7, forecast.getNetChange(ResourceType.WOOD));
        assertEquals(-6, forecast.getNetChange(ResourceType.FOOD));
    }

    @Test
    void settlementUpkeepShouldReduceWoodAndStoneForecast() {
        GameState gameState = createForestGameState();
        gameState.addSettlement(new HexCoordinate(0, 1));

        ResourceFlowForecast forecast =
                new EconomyForecastService(gameState)
                        .calculate();

        assertEquals(3, forecast.getNetChange(ResourceType.WOOD));
        assertEquals(-1, forecast.getNetChange(ResourceType.STONE));
    }

    private GameState createForestGameState() {
        Map<HexCoordinate, HexTile> tiles =
                new LinkedHashMap<>();

        for (HexCoordinate coordinate :
                CENTER.getCoordinatesWithinRadius(2)) {
            if (coordinate.equals(BUILDING_POSITION)) {
                tiles.put(
                        coordinate,
                        HexTile.withResourceAmounts(
                                coordinate,
                                TerrainType.FOREST,
                                Collections.singletonMap(
                                        NaturalResourceType.WOOD,
                                        100
                                )
                        )
                );
            } else {
                tiles.put(
                        coordinate,
                        new HexTile(
                                coordinate,
                                TerrainType.PLAIN
                        )
                );
            }
        }

        return new GameState(new GameMap(2, 470L, tiles));
    }
}
