package ir.ac.ap.civilization.model.game;

import ir.ac.ap.civilization.model.entity.Owner;
import ir.ac.ap.civilization.model.entity.OwnerType;
import ir.ac.ap.civilization.model.map.HexCoordinate;
import ir.ac.ap.civilization.model.map.HexTile;
import ir.ac.ap.civilization.model.map.TerrainType;
import ir.ac.ap.civilization.model.tribe.Tribe;
import ir.ac.ap.civilization.model.tribe.TribeRelationState;
import ir.ac.ap.civilization.model.unit.MilitaryUnit;
import ir.ac.ap.civilization.model.unit.NavalUnit;
import ir.ac.ap.civilization.model.unit.Unit;
import ir.ac.ap.civilization.model.unit.UnitMovementService;
import ir.ac.ap.civilization.model.unit.UnitPathfinder;
import ir.ac.ap.civilization.model.unit.UnitType;

import java.util.ArrayList;
import java.util.List;

public final class EnemyAiService {
    private final GameState gameState;
    private final UnitMovementService movement;
    private final CombatService combat;
    private final UnitPathfinder pathfinder;

    public EnemyAiService(GameState gameState) {
        this.gameState = gameState;
        this.movement = new UnitMovementService(gameState);
        this.combat = new CombatService(gameState);
        this.pathfinder = new UnitPathfinder(gameState);
    }

    public void advanceTurn() {
        spawnHostileTribeForces();
        spawnPirateIfNeeded();
        for (Unit enemy : new ArrayList<>(gameState.getUnits())) {
            if (enemy.getOwner().isPlayer()) continue;
            Unit target = nearestPlayerUnit(enemy.getPosition());
            if (target == null) continue;
            if (enemy.getPosition().distanceTo(target.getPosition()) <=
                    Math.max(1, enemy.getUnitType().getAttackRange())) {
                try { combat.attack(enemy.getPosition(), target.getPosition()); }
                catch (RuntimeException ignored) { /* another enemy may have removed target */ }
                continue;
            }
            moveOneStep(enemy, target.getPosition());
        }
    }

    private void spawnHostileTribeForces() {
        if (gameState.getTurnNumber() % 3 != 0) return;
        for (Tribe tribe : gameState.getTribes()) {
            if (tribe.isDefeated() ||
                    tribe.getRelationState() != TribeRelationState.ENEMY ||
                    hasOwnerUnits(Owner.tribe(tribe.getId()))) continue;
            UnitType enemyType;
            switch (gameState.getDifficulty()) {
                case EASY: enemyType = UnitType.SWORDSMAN; break;
                case HARD: enemyType = UnitType.CAVALRY; break;
                default: enemyType = UnitType.ARCHER;
            }
            int hp = (int) Math.floor(enemyType.getMaximumHitPoints() *
                    gameState.getDifficulty().getEnemyStrengthMultiplier());
            hp = Math.min(enemyType.getMaximumHitPoints(), hp);
            gameState.addUnit(new MilitaryUnit(
                    gameState.reserveUnitId(), enemyType,
                    tribe.getCampPosition(), Owner.tribe(tribe.getId()), hp
            ));
            gameState.getPhaseTwoState().notifyPlayer(
                    tribe.getName() + " recruited a " + enemyType.getDisplayName()
            );
        }
    }

    private void spawnPirateIfNeeded() {
        int interval;
        switch (gameState.getDifficulty()) {
            case EASY: interval = 8; break;
            case HARD: interval = 4; break;
            default: interval = 6;
        }
        if (gameState.getTurnNumber() % interval != 0 || hasPirate()) return;
        List<HexTile> seaTiles = new ArrayList<>();
        for (HexTile tile : gameState.getGameMap().getTiles()) {
            if (tile.getTerrainType() == TerrainType.SEA) seaTiles.add(tile);
        }
        if (seaTiles.isEmpty()) return;
        HexTile tile = seaTiles.get(gameState.getRandomSource().nextInt(seaTiles.size()));
        gameState.addUnit(new NavalUnit(
                gameState.reserveUnitId(), UnitType.PIRATE,
                tile.getCoordinate(), Owner.barbarian(99),
                UnitType.PIRATE.getMaximumHitPoints()
        ));
        gameState.getPhaseTwoState().notifyPlayer("Pirates appeared at sea");
    }

    private void moveOneStep(Unit enemy, HexCoordinate destination) {
        List<HexCoordinate> route = pathfinder.findPath(enemy, destination);
        if (!route.isEmpty()) {
            HexCoordinate next = route.get(0);
            try { movement.moveUnit(enemy.getId(), next); }
            catch (RuntimeException ignored) { }
        }
    }

    private Unit nearestPlayerUnit(HexCoordinate from) {
        Unit nearest = null;
        int distance = Integer.MAX_VALUE;
        for (Unit unit : gameState.getUnits()) {
            if (!unit.getOwner().isPlayer()) continue;
            int candidate = from.distanceTo(unit.getPosition());
            if (candidate < distance) { nearest = unit; distance = candidate; }
        }
        return nearest;
    }

    private boolean hasOwnerUnits(Owner owner) {
        for (Unit unit : gameState.getUnits()) {
            if (unit.getOwner().equals(owner)) return true;
        }
        return false;
    }

    private boolean hasPirate() {
        for (Unit unit : gameState.getUnits()) {
            if (unit.getUnitType() == UnitType.PIRATE &&
                    unit.getOwner().getType() != OwnerType.PLAYER) return true;
        }
        return false;
    }
}
