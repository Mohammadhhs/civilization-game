package ir.ac.ap.civilization.view;

import ir.ac.ap.civilization.controller.GameController;
import ir.ac.ap.civilization.model.game.GameState;
import ir.ac.ap.civilization.model.resource.ResourceType;
import ir.ac.ap.civilization.model.trade.BazaarTradeLevel;
import ir.ac.ap.civilization.model.tribe.Tribe;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public final class PhaseTwoDialog extends JDialog {
    private final GameController controller;
    private final JTextArea overview = new JTextArea();

    public PhaseTwoDialog(JFrame owner, GameController controller) {
        super(owner, "Phase II Command Center", true);
        this.controller = controller;
        setSize(700, 560);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(8, 8));
        getRootPane().setBorder(new EmptyBorder(10, 10, 10, 10));

        overview.setEditable(false);
        overview.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        add(new JScrollPane(overview), BorderLayout.CENTER);

        JPanel actions = new JPanel(new GridLayout(2, 3, 8, 8));
        JButton trade = new JButton("Bazaar Trade");
        JButton gift = new JButton("Gift Tribe");
        JButton mission = new JButton("Accept Mission");
        JButton war = new JButton("Declare War");
        JButton peace = new JButton("Request Peace");
        JButton alliance = new JButton("Request Alliance");
        for (JButton button : new JButton[]{trade, gift, mission, war, peace, alliance}) {
            button.setForeground(Color.BLACK);
            actions.add(button);
        }
        add(actions, BorderLayout.SOUTH);

        trade.addActionListener(event -> trade());
        gift.addActionListener(event -> withTribe(tribe ->
                controller.getTribeService().sendGift(
                        tribe.getId(), ResourceType.WOOD, 10)));
        mission.addActionListener(event -> withTribe(tribe ->
                controller.getTribeService().acceptMission(tribe.getId())));
        war.addActionListener(event -> withTribe(tribe ->
                controller.getTribeService().declareWar(tribe.getId())));
        peace.addActionListener(event -> withTribe(tribe ->
                controller.getTribeService().requestPeace(tribe.getId())));
        alliance.addActionListener(event -> withTribe(tribe ->
                controller.getTribeService().requestAlliance(tribe.getId())));
        refresh();
    }

    private void trade() {
        JComboBox<ResourceType> sold = new JComboBox<>(ResourceType.values());
        JComboBox<ResourceType> received = new JComboBox<>(ResourceType.values());
        JComboBox<BazaarTradeLevel> level = new JComboBox<>(BazaarTradeLevel.values());
        JPanel panel = new JPanel(new GridLayout(3, 2, 6, 6));
        panel.add(new JLabel("Sell:")); panel.add(sold);
        panel.add(new JLabel("Receive:")); panel.add(received);
        panel.add(new JLabel("Level:")); panel.add(level);
        if (JOptionPane.showConfirmDialog(this, panel, "Bazaar Trade",
                JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) return;
        runAction(() -> controller.tradeAtBazaar(
                (ResourceType) sold.getSelectedItem(),
                (ResourceType) received.getSelectedItem(),
                (BazaarTradeLevel) level.getSelectedItem()));
    }

    private void withTribe(TribeAction action) {
        GameState state = controller.getGameState();
        if (state.getTribes().isEmpty()) return;
        Tribe selected = (Tribe) JOptionPane.showInputDialog(
                this, "Choose tribe", "Tribe Interaction",
                JOptionPane.PLAIN_MESSAGE, null,
                state.getTribes().toArray(), state.getTribes().get(0));
        if (selected != null) runAction(() -> action.run(selected));
    }

    private void runAction(Runnable action) {
        try { action.run(); refresh(); }
        catch (RuntimeException exception) {
            JOptionPane.showMessageDialog(this, exception.getMessage(),
                    "Action unavailable", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void refresh() {
        GameState state = controller.getGameState();
        StringBuilder text = new StringBuilder();
        text.append("Season: ").append(state.getPhaseTwoState().getSeason())
                .append("  Happiness: ")
                .append(state.getPhaseTwoState().getHappiness().getValue())
                .append(" (").append(state.getPhaseTwoState().getHappiness().getLevel())
                .append(")\nMilitary: ").append(state.getActiveMilitaryUnitCount())
                .append('/').append(state.getMilitaryUnitCap()).append("\n\nTRIBES\n");
        for (Tribe tribe : state.getTribes()) {
            text.append(tribe.isDiscovered() ? "[seen] " : "[fog] ")
                    .append(tribe.getName()).append("  relation=")
                    .append(tribe.getRelation()).append(" ")
                    .append(tribe.getRelationState()).append("  campHP=")
                    .append(tribe.getCurrentHitPoints()).append('\n');
        }
        text.append("\nNOTIFICATIONS\n");
        for (String notification : state.getPhaseTwoState().getNotifications()) {
            text.append("- ").append(notification).append('\n');
        }
        overview.setText(text.toString());
    }

    private interface TribeAction { void run(Tribe tribe); }
}
