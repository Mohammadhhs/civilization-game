package ir.ac.ap.civilization.model.game;

import ir.ac.ap.civilization.model.entity.Damageable;
import ir.ac.ap.civilization.model.map.HexCoordinate;
import ir.ac.ap.civilization.model.random.RandomSource;
import ir.ac.ap.civilization.model.unit.Unit;
import ir.ac.ap.civilization.model.unit.UnitType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public final class CombatService {

    public static final int ATTACK_ACTION_POINT_COST = 1;
    public static final int DAMAGE_PER_LOST_DIE = 35;
    public static final int STRUCTURE_DAMAGE = 10;
    public static final int CATAPULT_STRUCTURE_DAMAGE = 20;

    private final GameState gameState;

    public CombatService(GameState gameState) {
        this.gameState = Objects.requireNonNull(
                gameState,
                "Game state cannot be null"
        );
    }

    public CombatResult attack(
            HexCoordinate attackerHex,
            HexCoordinate defenderHex
    ) {
        Objects.requireNonNull(attackerHex, "Attacker hex cannot be null");
        Objects.requireNonNull(defenderHex, "Defender hex cannot be null");

        int distance = attackerHex.distanceTo(defenderHex);
        if (distance < 1 || distance > 2) {
            throw new IllegalArgumentException("Attack range must be 1 or 2 hexes");
        }

        List<Unit> attackers = selectAttackers(attackerHex, distance);
        if (attackers.isEmpty()) {
            throw new IllegalStateException("No eligible military attacker");
        }
        List<Unit> defenders = selectDefenders(
                defenderHex,
                attackers.get(0)
        );
        if (defenders.isEmpty()) {
            throw new IllegalStateException("No hostile military defender");
        }

        gameState.beginOperation(GameOperation.RESOLVING_COMBAT);
        try {
            for (Unit attacker : attackers) {
                attacker.spendActionPoints(ATTACK_ACTION_POINT_COST);
            }

            List<Integer> attackDice = rollDice(
                    countDistinctCombatTypes(attackers)
            );
            int defenseCount = defenders.get(0).getOwner().getType().name()
                    .equals("WILDLIFE") ? 1 : 2;
            List<Integer> defenseDice = rollDice(defenseCount);

            int comparisons = Math.min(attackDice.size(), defenseDice.size());
            int attackerLosses = 0;
            int defenderLosses = 0;
            for (int index = 0; index < comparisons; index++) {
                if (attackDice.get(index) > defenseDice.get(index)) {
                    defenderLosses++;
                } else {
                    attackerLosses++;
                }
            }

            List<Integer> destroyed = new ArrayList<>();
            applyLosses(attackers, attackerLosses, destroyed);
            applyLosses(defenders, defenderLosses, destroyed);
            gameState.completeOperation();
            return new CombatResult(
                    attackDice,
                    defenseDice,
                    attackerLosses,
                    defenderLosses,
                    destroyed
            );
        } catch (RuntimeException exception) {
            gameState.failOperation();
            throw exception;
        }
    }

    public int attackStructure(
            HexCoordinate attackerHex,
            HexCoordinate targetHex,
            Damageable structure
    ) {
        Objects.requireNonNull(structure, "Structure cannot be null");
        if (!attackerHex.isAdjacentTo(targetHex)) {
            throw new IllegalArgumentException("Structure must be adjacent");
        }
        List<Unit> attackers = selectAttackers(attackerHex, 1);
        if (attackers.isEmpty()) {
            throw new IllegalStateException("No eligible military attacker");
        }
        int damage = 0;
        for (Unit attacker : attackers) {
            attacker.spendActionPoints(ATTACK_ACTION_POINT_COST);
            damage += attacker.getUnitType() == UnitType.CATAPULT
                    ? CATAPULT_STRUCTURE_DAMAGE
                    : STRUCTURE_DAMAGE;
        }
        structure.takeDamage(damage);
        return damage;
    }

    private List<Unit> selectAttackers(HexCoordinate coordinate, int distance) {
        List<Unit> result = new ArrayList<>();
        for (Unit unit : gameState.getUnitsAt(coordinate)) {
            if (!unit.getUnitType().isMilitary() ||
                    !unit.canSpendActionPoints(ATTACK_ACTION_POINT_COST)) {
                continue;
            }
            if (distance == 2 && unit.getUnitType() != UnitType.ARCHER &&
                    unit.getUnitType() != UnitType.CATAPULT) {
                continue;
            }
            result.add(unit);
        }
        return result;
    }

    private List<Unit> selectDefenders(HexCoordinate coordinate, Unit attacker) {
        List<Unit> result = new ArrayList<>();
        for (Unit unit : gameState.getUnitsAt(coordinate)) {
            if (unit.getUnitType().isMilitary() &&
                    !unit.getOwner().equals(attacker.getOwner())) {
                result.add(unit);
            }
        }
        return result;
    }

    private int countDistinctCombatTypes(List<Unit> units) {
        Set<UnitType> types = EnumSet.noneOf(UnitType.class);
        for (Unit unit : units) {
            if (unit.getUnitType() != UnitType.CATAPULT) {
                types.add(unit.getUnitType());
            }
        }
        return Math.max(1, types.size());
    }

    private List<Integer> rollDice(int count) {
        RandomSource random = gameState.getRandomSource();
        List<Integer> dice = new ArrayList<>();
        for (int index = 0; index < count; index++) {
            dice.add(random.nextInt(6) + 1);
        }
        Collections.sort(dice, Collections.reverseOrder());
        return dice;
    }

    private void applyLosses(
            List<Unit> units,
            int losses,
            List<Integer> destroyed
    ) {
        List<Unit> ordered = new ArrayList<>(units);
        Collections.sort(ordered, Comparator.comparingInt(
                unit -> priority(unit.getUnitType())
        ));
        int remaining = losses;
        for (Unit unit : ordered) {
            if (remaining <= 0) {
                break;
            }
            unit.takeDamage(DAMAGE_PER_LOST_DIE);
            remaining--;
            if (unit.isDestroyed()) {
                destroyed.add(unit.getId());
                gameState.removeUnit(unit);
            }
        }
    }

    private int priority(UnitType type) {
        switch (type) {
            case SWORDSMAN: return 0;
            case ARCHER: return 1;
            case CAVALRY: return 2;
            case CATAPULT: return 3;
            default: return 4;
        }
    }
}
