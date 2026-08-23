package ir.ac.ap.civilization.model.map;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HexCoordinateTest {

    @Test
    void getNeighborsShouldReturnSixUniqueCoordinates() {
        HexCoordinate center = new HexCoordinate(0, 0);

        List<HexCoordinate> neighbors = center.getNeighbors();

        Set<HexCoordinate> expectedNeighbors = new HashSet<>(
                Arrays.asList(
                        new HexCoordinate(1, 0),
                        new HexCoordinate(1, -1),
                        new HexCoordinate(0, -1),
                        new HexCoordinate(-1, 0),
                        new HexCoordinate(-1, 1),
                        new HexCoordinate(0, 1)
                )
        );

        assertEquals(6, neighbors.size());
        assertEquals(expectedNeighbors, new HashSet<>(neighbors));
    }

    @Test
    void distanceToSameCoordinateShouldBeZero() {
        HexCoordinate coordinate = new HexCoordinate(3, -2);

        int distance = coordinate.distanceTo(coordinate);

        assertEquals(0, distance);
    }

    @Test
    void distanceShouldBeCalculatedCorrectlyAndSymmetrically() {
        HexCoordinate first = new HexCoordinate(0, 0);
        HexCoordinate second = new HexCoordinate(2, -1);

        int firstToSecondDistance = first.distanceTo(second);
        int secondToFirstDistance = second.distanceTo(first);

        assertEquals(2, firstToSecondDistance);
        assertEquals(firstToSecondDistance, secondToFirstDistance);
    }

    @Test
    void isAdjacentToShouldOnlyAcceptCoordinatesAtDistanceOne() {
        HexCoordinate center = new HexCoordinate(0, 0);
        HexCoordinate adjacent = new HexCoordinate(1, 0);
        HexCoordinate farAway = new HexCoordinate(2, 0);

        assertTrue(center.isAdjacentTo(adjacent));
        assertFalse(center.isAdjacentTo(center));
        assertFalse(center.isAdjacentTo(farAway));
    }

    @Test
    void coordinatesWithinRadiusShouldHaveCorrectSizes() {
        HexCoordinate center = new HexCoordinate(0, 0);

        assertEquals(1, center.getCoordinatesWithinRadius(0).size());
        assertEquals(7, center.getCoordinatesWithinRadius(1).size());
        assertEquals(19, center.getCoordinatesWithinRadius(2).size());
    }

    @Test
    void coordinatesWithinRadiusShouldNotContainDistantCoordinates() {
        HexCoordinate center = new HexCoordinate(4, -2);
        int radius = 2;

        List<HexCoordinate> coordinates =
                center.getCoordinatesWithinRadius(radius);

        for (HexCoordinate coordinate : coordinates) {
            assertTrue(
                    center.distanceTo(coordinate) <= radius,
                    "Coordinate is outside the requested radius: " + coordinate
            );
        }
    }

    @Test
    void negativeRadiusShouldThrowException() {
        HexCoordinate center = new HexCoordinate(0, 0);

        assertThrows(
                IllegalArgumentException.class,
                () -> center.getCoordinatesWithinRadius(-1)
        );
    }

    @Test
    void distanceToNullShouldThrowException() {
        HexCoordinate center = new HexCoordinate(0, 0);

        assertThrows(
                NullPointerException.class,
                () -> center.distanceTo(null)
        );
    }

    @Test
    void equalCoordinatesShouldHaveEqualHashCodes() {
        HexCoordinate first = new HexCoordinate(2, -3);
        HexCoordinate second = new HexCoordinate(2, -3);
        HexCoordinate different = new HexCoordinate(2, -2);

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());

        assertNotEquals(first, different);
        assertNotEquals(first, null);
        assertNotEquals(first, "not a coordinate");
    }

    @Test
    void returnedNeighborListShouldBeUnmodifiable() {
        HexCoordinate center = new HexCoordinate(0, 0);
        List<HexCoordinate> neighbors = center.getNeighbors();

        assertThrows(
                UnsupportedOperationException.class,
                () -> neighbors.add(new HexCoordinate(5, 5))
        );
    }
}