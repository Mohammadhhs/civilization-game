package ir.ac.ap.civilization.view;

import ir.ac.ap.civilization.controller.GameController;
import ir.ac.ap.civilization.model.game.CombatResult;
import ir.ac.ap.civilization.model.map.HexCoordinate;
import ir.ac.ap.civilization.model.unit.Unit;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.LinkedHashSet;
import java.util.Set;

public final class CombatDialog extends JDialog {
    private final GameController controller;
    private final JComboBox<HexCoordinate> attackerBox;
    private final JComboBox<HexCoordinate> defenderBox;
    private final JLabel dice = new JLabel("Choose two hexes", SwingConstants.CENTER);
    private final JTextArea report = new JTextArea();
    private final JButton attackButton = new JButton("Attack (1 AP)");

    public CombatDialog(JFrame owner, GameController controller) {
        super(owner, "Military Combat", true);
        this.controller = controller;
        Set<HexCoordinate> attackers = new LinkedHashSet<>();
        Set<HexCoordinate> defenders = new LinkedHashSet<>();
        for (Unit unit : controller.getGameState().getUnits()) {
            if (!unit.getUnitType().isMilitary()) continue;
            if (unit.getOwner().isPlayer()) attackers.add(unit.getPosition());
            else defenders.add(unit.getPosition());
        }
        attackerBox = new JComboBox<>(attackers.toArray(new HexCoordinate[0]));
        defenderBox = new JComboBox<>(defenders.toArray(new HexCoordinate[0]));

        setLayout(new BorderLayout(10, 10));
        getRootPane().setBorder(new EmptyBorder(12, 12, 12, 12));
        JPanel selection = new JPanel(new GridLayout(2, 2, 8, 8));
        selection.add(new JLabel("Attacker hex:")); selection.add(attackerBox);
        selection.add(new JLabel("Defender hex:")); selection.add(defenderBox);
        dice.setFont(new Font(Font.MONOSPACED, Font.BOLD, 20));
        report.setEditable(false);
        report.setLineWrap(true);
        report.setWrapStyleWord(true);
        attackButton.setForeground(Color.BLACK);
        attackButton.setEnabled(attackerBox.getItemCount() > 0 &&
                defenderBox.getItemCount() > 0);
        attackButton.setToolTipText(attackButton.isEnabled()
                ? "Resolve deterministic six-sided dice combat"
                : "Use Evaluation Scenarios to prepare hostile units first");
        attackButton.addActionListener(event -> animateAndAttack());

        JPanel top = new JPanel(new BorderLayout(8, 8));
        top.add(selection, BorderLayout.NORTH);
        top.add(dice, BorderLayout.CENTER);
        add(top, BorderLayout.NORTH);
        add(new JScrollPane(report), BorderLayout.CENTER);
        add(attackButton, BorderLayout.SOUTH);
        setSize(560, 420);
        setLocationRelativeTo(owner);
    }

    private void animateAndAttack() {
        attackButton.setEnabled(false);
        final int[] ticks = {0};
        Timer animation = new Timer(90, null);
        animation.addActionListener(event -> {
            ticks[0]++;
            dice.setText("Rolling...  [" + (1 + ticks[0] % 6) + "] vs [" +
                    (1 + ticks[0] * 3 % 6) + "]");
            if (ticks[0] < 8) return;
            animation.stop();
            resolveAttack();
        });
        animation.start();
    }

    private void resolveAttack() {
        try {
            CombatResult result = controller.attack(
                    (HexCoordinate) attackerBox.getSelectedItem(),
                    (HexCoordinate) defenderBox.getSelectedItem()
            );
            dice.setText(result.getAttackDice() + "  vs  " + result.getDefenseDice());
            report.setText("Attacker losses: " + result.getAttackerLosses() +
                    "\nDefender losses: " + result.getDefenderLosses() +
                    "\nDestroyed unit IDs: " + result.getDestroyedUnitIds() +
                    "\nTies are resolved in favor of the defender.");
        } catch (RuntimeException exception) {
            dice.setText("Attack unavailable");
            report.setText(exception.getMessage());
        } finally {
            attackButton.setEnabled(true);
        }
    }
}
