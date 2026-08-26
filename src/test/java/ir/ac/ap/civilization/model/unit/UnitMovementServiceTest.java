package ir.ac.ap.civilization.model.unit;

import ir.ac.ap.civilization.model.game.GameState;
import ir.ac.ap.civilization.model.map.HexCoordinate;
import ir.ac.ap.civilization.model.map.HexTile;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UnitMovementServiceTest {

    @Test
    void validMovementChangesPositionAndConsumesTerrainCost() {
        GameState gameState = new GameState();
        UnitMovementService movementService =
                new UnitMovementService(gameState);

        Unit builder = gameState.getUnitById(1)
                .orElseThrow(AssertionError::new);

        HexCoordinate start = builder.getPosition();
        HexCoordinate destination =
                gameState.getTownHallCoordinate();

        UnitMovementResult result =
                movementService.moveUnit(
                        builder.getId(),
                        destination
                );

        assertEquals(start, result.getStart());
        assertEquals(destination, result.getDestination());
        assertEquals(1, result.getActionPointCost());
        assertEquals(4, result.getRemainingActionPoints());

        assertEquals(destination, builder.getPosition());
        assertEquals(4, builder.getCurrentActionPoints());
    }

    @Test
    void forestDestinationUsesForestMovementCost() {
        GameState gameState = new GameState();
        UnitMovementService movementService =
                new UnitMovementService(gameState);

        Unit secondBuilder = gameState.getUnitById(2)
                .orElseThrow(AssertionError::new);

        HexCoordinate startingForest =
                new HexCoordinate(1, 0);

        UnitMovementResult result =
                movementService.moveUnit(
                        secondBuilder.getId(),
                        startingForest
                );

        assertEquals(2, result.getActionPointCost());
        assertEquals(3, secondBuilder.getCurrentActionPoints());
        assertEquals(startingForest, secondBuilder.getPosition());
    }

    @Test
    void severalUnitsMayShareTheSameDestinationHex() {
        GameState gameState = new GameState();
        UnitMovementService movementService =
                new UnitMovementService(gameState);

        Unit firstBuilder = gameState.getUnitById(1)
                .orElseThrow(AssertionError::new);
        Unit secondBuilder = gameState.getUnitById(2)
                .orElseThrow(AssertionError::new);
        HexCoordinate destination =
                gameState.getTownHallCoordinate();

        movementService.moveUnit(
                firstBuilder.getId(),
                destination
        );
        movementService.moveUnit(
                secondBuilder.getId(),
                destination
        );

        assertEquals(
                2,
                gameState.getUnitsAt(destination).size()
        );
        assertTrue(
                gameState.getUnitsAt(destination)
                        .contains(firstBuilder)
        );
        assertTrue(
                gameState.getUnitsAt(destination)
                        .contains(secondBuilder)
        );
    }

    @Test
    void nonAdjacentMovementIsRejectedAtomically() {
        GameState gameState = new GameState();
        UnitMovementService movementService =
                new UnitMovementService(gameState);

        Unit builder = gameState.getUnitById(1)
                .orElseThrow(AssertionError::new);

        HexCoordinate originalPosition = builder.getPosition();
        int originalActionPoints =
                builder.getCurrentActionPoints();

        assertThrows(
                IllegalArgumentException.class,
                () -> movementService.moveUnit(
                        builder.getId(),
                        new HexCoordinate(3, 0)
                )
        );

        assertEquals(originalPosition, builder.getPosition());
        assertEquals(
                originalActionPoints,
                builder.getCurrentActionPoints()
        );
    }

    @Test
    void destinationOutsideMapIsRejectedAtomically() {
        GameState gameState = new GameState();
        UnitMovementService movementService =
                new UnitMovementService(gameState);

        Unit builder = gameState.getUnitById(1)
                .orElseThrow(AssertionError::new);

        HexCoordinate originalPosition = builder.getPosition();
        int originalActionPoints =
                builder.getCurrentActionPoints();

        assertThrows(
                IllegalArgumentException.class,
                () -> movementService.moveUnit(
                        builder.getId(),
                        new HexCoordinate(100, 100)
                )
        );

        assertEquals(originalPosition, builder.getPosition());
        assertEquals(
                originalActionPoints,
                builder.getCurrentActionPoints()
        );
    }

    @Test
    void impassableTerrainIsRejectedAtomically() {
        GameState gameState = new GameState();
        UnitMovementService movementService =
                new UnitMovementService(gameState);

        Unit explorer = gameState.getUnitById(5)
                .orElseThrow(AssertionError::new);

        HexCoordinate[] movementPair =
                findPassableNeighborAndBlockedDestination(
                        gameState
                );

        explorer.moveTo(movementPair[0]);

        HexCoordinate originalPosition = explorer.getPosition();
        int originalActionPoints =
                explorer.getCurrentActionPoints();

        assertThrows(
                IllegalStateException.class,
                () -> movementService.moveUnit(
                        explorer.getId(),
                        movementPair[1]
                )
        );

        assertEquals(originalPosition, explorer.getPosition());
        assertEquals(
                originalActionPoints,
                explorer.getCurrentActionPoints()
        );
    }

    @Test
    void insufficientActionPointsRejectMovementAtomically() {
        GameState gameState = new GameState();
        UnitMovementService movementService =
                new UnitMovementService(gameState);

        Unit builder = gameState.getUnitById(1)
                .orElseThrow(AssertionError::new);

        builder.spendActionPoints(
                builder.getMaximumActionPoints()
        );

        HexCoordinate originalPosition = builder.getPosition();

        assertThrows(
                IllegalStateException.class,
                () -> movementService.moveUnit(
                        builder.getId(),
                        gameState.getTownHallCoordinate()
                )
        );

        assertEquals(originalPosition, builder.getPosition());
        assertEquals(0, builder.getCurrentActionPoints());
    }

    @Test
    void validDestinationsAreAdjacentPassableAndAffordable() {
        GameState gameState = new GameState();
        UnitMovementService movementService =
                new UnitMovementService(gameState);

        Unit worker = gameState.getUnitById(3)
                .orElseThrow(AssertionError::new);

        List<HexTile> validDestinations =
                movementService.getValidMovementDestinations(
                        worker.getId()
                );

        assertFalse(validDestinations.isEmpty());

        for (HexTile destination : validDestinations) {
            assertTrue(
                    worker.getPosition().isAdjacentTo(
                            destination.getCoordinate()
                    )
            );
            assertTrue(destination.isNormallyPassable());
            assertTrue(
                    worker.canSpendActionPoints(
                            destination.getBaseMovementCost()
                    )
            );
            assertTrue(
                    movementService.canMoveUnit(
                            worker.getId(),
                            destination.getCoordinate()
                    )
            );
        }
    }

    @Test
    void unknownUnitCannotMove() {
        GameState gameState = new GameState();
        UnitMovementService movementService =
                new UnitMovementService(gameState);

        HexCoordinate destination =
                gameState.getTownHallCoordinate();

        assertFalse(
                movementService.canMoveUnit(
                        999,
                        destination
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> movementService.moveUnit(
                        999,
                        destination
                )
        );
    }

    private HexCoordinate[]
    findPassableNeighborAndBlockedDestination(
            GameState gameState
    ) {
        for (HexTile blockedTile :
                gameState.getGameMap().getTiles()) {

            if (blockedTile.isNormallyPassable()) {
                continue;
            }

            for (HexTile neighbor :
                    gameState.getGameMap().getValidNeighbors(
                            blockedTile.getCoordinate()
                    )) {

                if (neighbor.isNormallyPassable()) {
                    return new HexCoordinate[]{
                            neighbor.getCoordinate(),
                            blockedTile.getCoordinate()
                    };
                }
            }
        }

        throw new AssertionError(
                "Generated map has no reachable blocked terrain"
        );
    }
}
