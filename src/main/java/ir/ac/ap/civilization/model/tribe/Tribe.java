package ir.ac.ap.civilization.model.tribe;

import ir.ac.ap.civilization.model.entity.Damageable;
import ir.ac.ap.civilization.model.entity.EntityKey;
import ir.ac.ap.civilization.model.entity.EntityKind;
import ir.ac.ap.civilization.model.entity.GameEntity;
import ir.ac.ap.civilization.model.entity.HitPoints;
import ir.ac.ap.civilization.model.map.HexCoordinate;

import java.util.Objects;
import java.util.Optional;

public final class Tribe implements GameEntity, Damageable {
    private final int id;
    private final String name;
    private final TribeType type;
    private final HexCoordinate campPosition;
    private final HitPoints campHitPoints = new HitPoints(200);
    private int relation;
    private boolean discovered;
    private boolean defeated;
    private TribeMission mission;

    public Tribe(int id, String name, TribeType type, HexCoordinate campPosition) {
        if (id <= 0) throw new IllegalArgumentException("Tribe id must be positive");
        this.id = id;
        this.name = Objects.requireNonNull(name, "Name cannot be null");
        this.type = Objects.requireNonNull(type, "Type cannot be null");
        this.campPosition = Objects.requireNonNull(campPosition, "Position cannot be null");
    }
    @Override public int getId() { return id; }
    @Override public EntityKey getEntityKey() { return new EntityKey(EntityKind.TRIBE, id); }
    public String getName() { return name; }
    public TribeType getType() { return type; }
    public HexCoordinate getCampPosition() { return campPosition; }
    public int getRelation() { return relation; }
    public TribeRelationState getRelationState() { return TribeRelationState.fromScore(relation); }
    public boolean isDiscovered() { return discovered; }
    public void discover() { discovered = true; }
    public boolean isDefeated() { return defeated; }
    public void changeRelation(int delta) { relation = Math.max(-100, Math.min(100, relation + delta)); }
    public void declareWar() { relation = -100; if (mission != null) mission.cancel(); }
    public void makePeace() { relation = -40; }
    public Optional<TribeMission> getMission() { return Optional.ofNullable(mission); }
    public void setMission(TribeMission mission) { this.mission = mission; }
    @Override public int getCurrentHitPoints() { return campHitPoints.getCurrent(); }
    @Override public int getMaximumHitPoints() { return campHitPoints.getMaximum(); }
    @Override public void takeDamage(int amount) {
        campHitPoints.damage(amount);
        if (campHitPoints.isDepleted()) defeated = true;
    }
    @Override public void heal(int amount) { campHitPoints.heal(amount); }
    @Override public String toString() { return name + " (" + getRelationState() + ")"; }
}
