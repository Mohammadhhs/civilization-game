package ir.ac.ap.civilization.model.infrastructure;

import ir.ac.ap.civilization.model.map.HexCoordinate;

import java.util.Objects;
import java.io.Serializable;

public final class HexEdge implements Serializable {
    private final HexCoordinate first;
    private final HexCoordinate second;

    public HexEdge(HexCoordinate one, HexCoordinate two) {
        Objects.requireNonNull(one, "First coordinate cannot be null");
        Objects.requireNonNull(two, "Second coordinate cannot be null");
        if (!one.isAdjacentTo(two)) {
            throw new IllegalArgumentException("An edge requires adjacent hexes");
        }
        if (compare(one, two) <= 0) { first = one; second = two; }
        else { first = two; second = one; }
    }
    public HexCoordinate getFirst() { return first; }
    public HexCoordinate getSecond() { return second; }
    @Override public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof HexEdge)) return false;
        HexEdge other = (HexEdge) object;
        return first.equals(other.first) && second.equals(other.second);
    }
    @Override public int hashCode() { return Objects.hash(first, second); }
    private static int compare(HexCoordinate a, HexCoordinate b) {
        int q = Integer.compare(a.getQ(), b.getQ());
        return q != 0 ? q : Integer.compare(a.getR(), b.getR());
    }
}
