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
        super("Civilization Game");

        this.controller = Objects.requireNonNull(
                controller,
                "Game controller cannot be null"
        );

        GameState gameState =
                controller.getGameState();

        this.hudPanel = new HudPanel();

        this.hexMapPanel = new HexMapPanel(
                gameState.getGameMap(),
                gameState.getTownHallCoordinate()
        );

        this.hexInfoPanel = new HexInfoPanel();

        configureFrame();
        createLayout();
        wireActions();
        wireMapSelection();
        refreshHud();
    }

    private void configureFrame() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setSize(1200, 760);
        setMinimumSize(new Dimension(960, 640));
        setLocationRelativeTo(null);
    }

    private void createLayout() {
        add(hudPanel, BorderLayout.NORTH);
        add(hexMapPanel, BorderLayout.CENTER);
        add(hexInfoPanel, BorderLayout.EAST);
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

    private void wireMapSelection() {
        GameState gameState =
                controller.getGameState();

        hexMapPanel.setSelectionListener(
                tile -> hexInfoPanel.showTile(
                        tile,
                        tile.getCoordinate().equals(
                                gameState
                                        .getTownHallCoordinate()
                        )
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