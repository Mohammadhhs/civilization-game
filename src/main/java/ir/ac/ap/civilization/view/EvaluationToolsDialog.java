package ir.ac.ap.civilization.view;

import ir.ac.ap.civilization.model.disaster.DisasterType;
import ir.ac.ap.civilization.model.game.EvaluationScenarioService;
import ir.ac.ap.civilization.model.game.GameState;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public final class EvaluationToolsDialog extends JDialog {
    private final EvaluationScenarioService scenarios;
    private final JTextArea result = new JTextArea();

    public EvaluationToolsDialog(JFrame owner, GameState gameState) {
        super(owner, "Evaluation / Debug Scenarios", true);
        scenarios = new EvaluationScenarioService(gameState);
        setLayout(new BorderLayout(8, 8));
        getRootPane().setBorder(new EmptyBorder(12, 12, 12, 12));

        JPanel buttons = new JPanel(new GridLayout(0, 2, 8, 8));
        addButton(buttons, "Add Resources", scenarios::addResources);
        addButton(buttons, "Prepare Combat", scenarios::prepareCombat);
        addButton(buttons, "Prepare Diplomacy", scenarios::prepareDiplomacy);
        addButton(buttons, "Show Infrastructure", scenarios::prepareInfrastructure);
        addButton(buttons, "Prepare Naval", scenarios::prepareNaval);
        addButton(buttons, "Advance Season", scenarios::advanceSeason);
        for (DisasterType type : DisasterType.values()) {
            addButton(buttons, "Trigger " + type,
                    () -> scenarios.triggerDisaster(type));
        }

        result.setEditable(false);
        result.setLineWrap(true);
        result.setWrapStyleWord(true);
        add(new JScrollPane(buttons), BorderLayout.CENTER);
        add(new JScrollPane(result), BorderLayout.SOUTH);
        setSize(650, 600);
        setLocationRelativeTo(owner);
    }

    private void addButton(JPanel panel, String title, Scenario action) {
        JButton button = new JButton(title);
        button.setForeground(Color.BLACK);
        button.addActionListener(event -> {
            try { result.setText(action.run()); }
            catch (RuntimeException exception) {
                result.setText("Scenario failed safely: " + exception.getMessage());
            }
        });
        panel.add(button);
    }

    private interface Scenario { String run(); }
}
