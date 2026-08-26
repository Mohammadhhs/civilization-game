package ir.ac.ap.civilization.model.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class CombatResult {

    private final List<Integer> attackDice;
    private final List<Integer> defenseDice;
    private final int attackerLosses;
    private final int defenderLosses;
    private final List<Integer> destroyedUnitIds;

    CombatResult(
            List<Integer> attackDice,
            List<Integer> defenseDice,
            int attackerLosses,
            int defenderLosses,
            List<Integer> destroyedUnitIds
    ) {
        this.attackDice = immutableCopy(attackDice);
        this.defenseDice = immutableCopy(defenseDice);
        this.attackerLosses = attackerLosses;
        this.defenderLosses = defenderLosses;
        this.destroyedUnitIds = immutableCopy(destroyedUnitIds);
    }

    public List<Integer> getAttackDice() { return attackDice; }
    public List<Integer> getDefenseDice() { return defenseDice; }
    public int getAttackerLosses() { return attackerLosses; }
    public int getDefenderLosses() { return defenderLosses; }
    public List<Integer> getDestroyedUnitIds() {
        return destroyedUnitIds;
    }

    private static List<Integer> immutableCopy(List<Integer> source) {
        return Collections.unmodifiableList(new ArrayList<>(source));
    }
}
