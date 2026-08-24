package ir.ac.ap.civilization.view;

import ir.ac.ap.civilization.controller.GameController;
import ir.ac.ap.civilization.model.game.GameState;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.Objects;

public final class GameFrame extends JFrame {

    private static final long serialVersionUID = 1L;

    private final GameController controller;
    private final HudPanel hudPanel;
    private final HexMapPanel hexMapPanel;
    private final HexInfoPanel hexInfoPanel;

    public GameFrame(GameController controller) {
        super("Civilization - Advanced Programming Project");

        this.controller = Objects.requireNonNull(
                controller,
                "Game controller cannot be null"
        );

        this.hudPanel = new HudPanel();
        this.hexMapPanel = new HexMapPanel(controller);
        this.hexInfoPanel = new HexInfoPanel();

        configureFrame();
        arrangeComponents();
        connectViewActions();
        refreshView();
    }

    private void configureFrame() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setMinimumSize(new Dimension(960, 640));
        setSize(1200, 760);
        setLocationRelativeTo(null);
    }

    private void arrangeComponents() {
        add(hudPanel, BorderLayout.NORTH);
        add(hexMapPanel, BorderLayout.CENTER);
        add(hexInfoPanel, BorderLayout.EAST);
    }

    private void connectViewActions() {
        GameState gameState = controller.getGameState();

        hexMapPanel.setSelectionListener(
                (tile, unit) -> hexInfoPanel.showSelection(
                        tile,
                        unit,
                        tile.getCoordinate().equals(
                                gameState.getTownHallCoordinate()
                        )
                )
        );

        hexMapPanel.setStatusListener(
                hudPanel::setStatusMessage
        );

        hudPanel.setStartUpgradeAction(
                event -> executeAction(
                        controller::startTownHallUpgrade,
                        "Town Hall upgrade started"
                )
        );

        hudPanel.setCancelCommandAction(
                event -> executeAction(
                        controller::cancelTownHallCommand,
                        "Current Town Hall command cancelled"
                )
        );

        hudPanel.setEndTurnAction(
                event -> executeAction(
                        () -> {
                            controller.endTurn();
                            hexMapPanel.refreshFromModel();
                        },
                        "Turn completed and unit AP restored"
                )
        );
    }

    private void executeAction(
            Runnable action,
            String successMessage
    ) {
        try {
            action.run();
            refreshView();
            hudPanel.setStatusMessage(
                    successMessage,
                    false
            );
        } catch (
                IllegalArgumentException |
                IllegalStateException exception
        ) {
            refreshView();

            hudPanel.setStatusMessage(
                    exception.getMessage(),
                    true
            );

            JOptionPane.showMessageDialog(
                    this,
                    exception.getMessage(),
                    "Action is not available",
                    JOptionPane.WARNING_MESSAGE
            );
        }
    }

    private void refreshView() {
        hudPanel.refresh(
                controller.getGameState()
        );

        hexMapPanel.repaint();
    }
}