package ir.ac.ap.civilization.view;

import ir.ac.ap.civilization.controller.GameController;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.util.Objects;

public final class GameFrame extends JFrame {

    private static final Color MAP_BACKGROUND_COLOR =
            new Color(24, 29, 36);

    private final GameController controller;
    private final HudPanel hudPanel;

    public GameFrame(GameController controller) {
        super("Civilization Game");

        this.controller = Objects.requireNonNull(
                controller,
                "Game controller cannot be null"
        );

        this.hudPanel = new HudPanel();

        configureFrame();
        createLayout();
        wireActions();
        refreshHud();
    }

    private void configureFrame() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setSize(1100, 700);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);
    }

    private void createLayout() {
        add(hudPanel, BorderLayout.NORTH);
        add(createMapPlaceholder(), BorderLayout.CENTER);
    }

    private JPanel createMapPlaceholder() {
        JPanel mapPanel =
                new JPanel(new GridBagLayout());

        mapPanel.setBackground(MAP_BACKGROUND_COLOR);

        mapPanel.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                new Color(55, 64, 75)
                        ),
                        new EmptyBorder(20, 20, 20, 20)
                )
        );

        JLabel placeholderLabel = new JLabel(
                "Hex Map - Next Milestone",
                SwingConstants.CENTER
        );

        placeholderLabel.setFont(
                new Font(Font.SANS_SERIF, Font.BOLD, 28)
        );

        placeholderLabel.setForeground(
                new Color(130, 142, 155)
        );

        mapPanel.add(placeholderLabel);

        return mapPanel;
    }

    private void wireActions() {
        hudPanel.setStartUpgradeAction(
                event -> executeAction(
                        controller::startTownHallUpgrade,
                        "Town Hall upgrade started"
                )
        );

        hudPanel.setCancelCommandAction(
                event -> executeAction(
                        controller::cancelTownHallCommand,
                        "Command cancelled; resources were not refunded"
                )
        );

        hudPanel.setEndTurnAction(
                event -> executeAction(
                        controller::endTurn,
                        "Turn completed"
                )
        );
    }

    private void executeAction(
            Runnable action,
            String successMessage
    ) {
        try {
            action.run();

            hudPanel.setStatusMessage(
                    successMessage,
                    false
            );
        } catch (
                IllegalStateException |
                IllegalArgumentException exception
        ) {
            hudPanel.setStatusMessage(
                    exception.getMessage(),
                    true
            );

            JOptionPane.showMessageDialog(
                    this,
                    exception.getMessage(),
                    "Action unavailable",
                    JOptionPane.WARNING_MESSAGE
            );
        } finally {
            refreshHud();
        }
    }

    private void refreshHud() {
        hudPanel.refresh(
                controller.getGameState()
        );
    }
}