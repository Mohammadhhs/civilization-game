package ir.ac.ap.civilization.model.game;

import ir.ac.ap.civilization.model.map.GameMap;
import ir.ac.ap.civilization.model.map.HexCoordinate;
import ir.ac.ap.civilization.model.map.HexTile;
import ir.ac.ap.civilization.model.map.TerrainType;
import ir.ac.ap.civilization.model.unit.Unit;
import ir.ac.ap.civilization.model.unit.UnitMovementService;
import ir.ac.ap.civilization.model.unit.UnitType;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BorderExpansionServiceTest {

    private static final HexCoordinate CENTER =
            new HexCoordinate(0, 0);

    @Test
    void borderExpanderShouldClaimCenterAndNeighbors() {
        GameState gameState = createGameState(4, true);
        Unit expander = gameState.deployUnit(
                UnitType.BORDER_EXPANDER
        );

        moveAlongQAxis(gameState, expander.getId(), 3);

        BorderExpansionService service =
                new BorderExpansionService(gameState);

        assertTrue(service.canExpand(expander.getId()));

        int claimedTiles = service.expand(expander.getId());

        assertEquals(6, claimedTiles);
        assertEquals(25, gameState.getPlayerTerritory().size());
        assertFalse(gameState.getUnitById(expander.getId()).isPresent());
        assertTrue(gameState.getPlayerTerritory().contains(
                new HexCoordinate(4, 0)
        ));
    }

    @Test
    void expansionAreaMustTouchExistingTerritory() {
        GameState gameState = createGameState(6, true);
        Unit expander = gameState.deployUnit(
                UnitType.BORDER_EXPANDER
        );

        moveAlongQAxis(gameState, expander.getId(), 5);

        BorderExpansionService service =
                new BorderExpansionService(gameState);

        assertFalse(service.canExpand(expander.getId()));
        assertThrows(
                IllegalStateException.class,
                () -> service.expand(expander.getId())
        );
        assertTrue(gameState.getUnitById(expander.getId()).isPresent());
        assertEquals(19, gameState.getPlayerTerritory().size());
    }

    @Test
    void ordinaryUnitCannotExpandBorder() {
        GameState gameState = createGameState(4, true);
        BorderExpansionService service =
                new BorderExpansionService(gameState);

        assertFalse(service.canExpand(1));
        assertThrows(
                IllegalArgumentException.class,
                () -> service.expand(1)
        );
    }

    @Test
    void everyClaimedHexMustBeExploredBeforeExpansion() {
        GameState gameState = createGameState(4, false);
        Unit expander = gameState.deployUnit(
                UnitType.BORDER_EXPANDER
        );

        moveAlongQAxis(gameState, expander.getId(), 3);

        BorderExpansionService service =
                new BorderExpansionService(gameState);

        assertFalse(service.canExpand(expander.getId()));
        assertThrows(
                IllegalStateException.class,
                () -> service.expand(expander.getId())
        );
        assertTrue(gameState.getUnitById(expander.getId()).isPresent());
    }

    @Test
    void alreadyOwnedAreaCannotConsumeExpander() {
        GameState gameState = createGameState(4, true);
        Unit expander = gameState.deployUnit(
                UnitType.BORDER_EXPANDER
        );

        BorderExpansionService service =
                new BorderExpansionService(gameState);

        assertFalse(service.canExpand(expander.getId()));
        assertTrue(gameState.getUnitById(expander.getId()).isPresent());
    }

    private void moveAlongQAxis(
            GameState gameState,
            int unitId,
            int destinationQ
    ) {
        UnitMovementService movementService =
                new UnitMovementService(gameState);

        for (int q = 1; q <= destinationQ; q++) {
            Unit unit = gameState.getUnitById(unitId)
                    .orElseThrow(AssertionError::new);

            if (!unit.canSpendActionPoints(1)) {
                unit.restoreActionPoints();
            }

            movementService.moveUnit(
                    unitId,
                    new HexCoordinate(q, 0)
            );
        }
    }

    private GameState createGameState(
            int radius,
            boolean discoverAll
    ) {
        Map<HexCoordinate, HexTile> tiles =
                new LinkedHashMap<>();

        for (HexCoordinate coordinate :
                CENTER.getCoordinatesWithinRadius(radius)) {
            HexTile tile = new HexTile(
                    coordinate,
                    TerrainType.PLAIN
            );

            if (discoverAll) {
                tile.discover();
            }

            tiles.put(coordinate, tile);
        }

        return new GameState(
                new GameMap(radius, 469L, tiles)
        );
    }
}
