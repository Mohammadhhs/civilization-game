package ir.ac.ap.civilization.model.game;

import ir.ac.ap.civilization.model.building.BuildingType;
import ir.ac.ap.civilization.model.building.ResourceBuilding;
import ir.ac.ap.civilization.model.map.NaturalResourceType;
import ir.ac.ap.civilization.model.unit.Unit;
import ir.ac.ap.civilization.model.unit.Worker;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GameStateIdleUnitTest {

    @Test
    void allStartingUnitsShouldInitiallyBeIdle() {
        GameState gameState = new GameState();

        assertEquals(5, gameState.getIdleUnits().size());
    }

    @Test
    void unitWithoutApAndStationedWorkerShouldNotBeIdle() {
        GameState gameState = new GameState();

        Unit builder = gameState.getUnitById(1)
                .orElseThrow(AssertionError::new);
        builder.spendActionPoints(
                builder.getCurrentActionPoints()
        );

        ResourceBuilding building =
                gameState.addResourceBuilding(
                        BuildingType.LUMBER_MILL,
                        new ir.ac.ap.civilization.model.map.HexCoordinate(
                                1, 0
                        ),
                        NaturalResourceType.WOOD
                );

        Worker worker = (Worker) gameState.getUnitById(3)
                .orElseThrow(AssertionError::new);
        building.stationWorker(worker.getId());
        worker.stationAt(building.getId());

        assertEquals(3, gameState.getIdleUnits().size());
    }
}
