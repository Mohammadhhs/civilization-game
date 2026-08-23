package ir.ac.ap.civilization.model.map;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class HexCoordinate {

    private static final int[][] DIRECTIONS = {
            {1, 0},
            {1, -1},
            {0, -1},
            {-1, 0},
            {-1, 1},
            {0, 1}
    };

    private final int q;
    private final int r;

    public HexCoordinate(int q, int r) {
        this.q = q;
        this.r = r;
    }

    public int getQ() {
        return q;
    }

    public int getR() {
        return r;
    }

    public List<HexCoordinate> getNeighbors() {
        List<HexCoordinate> neighbors = new ArrayList<>(6);

        for (int[] direction : DIRECTIONS) {
            int neighborQ = q + direction[0];
            int neighborR = r + direction[1];

            neighbors.add(new HexCoordinate(neighborQ, neighborR));
        }

        return Collections.unmodifiableList(neighbors);
    }

    public boolean isAdjacentTo(HexCoordinate other) {
        return distanceTo(other) == 1;
    }

    public int distanceTo(HexCoordinate other) {
        Objects.requireNonNull(other, "Other coordinate cannot be null");

        int thisS = -q - r;
        int otherS = -other.q - other.r;

        int qDifference = Math.abs(q - other.q);
        int rDifference = Math.abs(r - other.r);
        int sDifference = Math.abs(thisS - otherS);

        return (qDifference + rDifference + sDifference) / 2;
    }

    public List<HexCoordinate> getCoordinatesWithinRadius(int radius) {
        if (radius < 0) {
            throw new IllegalArgumentException("Radius cannot be negative");
        }

        List<HexCoordinate> coordinates = new ArrayList<>();

        for (int qOffset = -radius; qOffset <= radius; qOffset++) {
            int minimumROffset = Math.max(-radius, -qOffset - radius);
            int maximumROffset = Math.min(radius, -qOffset + radius);

            for (int rOffset = minimumROffset;
                 rOffset <= maximumROffset;
                 rOffset++) {

                coordinates.add(
                        new HexCoordinate(q + qOffset, r + rOffset)
                );
            }
        }

        return Collections.unmodifiableList(coordinates);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (!(object instanceof HexCoordinate)) {
            return false;
        }

        HexCoordinate other = (HexCoordinate) object;
        return q == other.q && r == other.r;
    }

    @Override
    public int hashCode() {
        return Objects.hash(q, r);
    }

    @Override
    public String toString() {
        return "HexCoordinate{" +
                "q=" + q +
                ", r=" + r +
                '}';
    }
}