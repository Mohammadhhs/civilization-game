package ir.ac.ap.civilization.model.infrastructure;

import ir.ac.ap.civilization.model.entity.Damageable;
import ir.ac.ap.civilization.model.entity.HitPoints;

public final class Wall implements Damageable {
    private final HexEdge edge;
    private final HitPoints hitPoints = new HitPoints(120);
    public Wall(HexEdge edge) { this.edge = edge; }
    public HexEdge getEdge() { return edge; }
    @Override public int getCurrentHitPoints() { return hitPoints.getCurrent(); }
    @Override public int getMaximumHitPoints() { return hitPoints.getMaximum(); }
    @Override public void takeDamage(int amount) { hitPoints.damage(amount); }
    @Override public void heal(int amount) { hitPoints.heal(amount); }
}
