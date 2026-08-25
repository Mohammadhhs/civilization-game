package ir.ac.ap.civilization.view;

import ir.ac.ap.civilization.controller.GameController;
import ir.ac.ap.civilization.model.game.GameState;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.Objects;

public final class GameFrame extends JFrame {

    private static final long serialVersionUID = 1L;

    private final GameController controller;
    private final HudPanel hudPanel;
    private final HexMapPanel hexMapPanel;
    private final HexInfoPanel hexInfoPanel;
    private final TechnologyPanel technologyPanel;
    private final BuildingActionsPanel buildingActionsPanel;

    public GameFrame(GameController controller) {
        super("Civilization - Advanced Programming Project");

        this.controller = Objects.requireNonNull(
                controller,
                "Game controller cannot be null"
        );

        this.hudPanel = new HudPanel();
        this.hexMapPanel = new HexMapPanel(controller);
        this.hexInfoPanel = new HexInfoPanel();
        this.technologyPanel = new TechnologyPanel();
        this.buildingActionsPanel =
                new BuildingActionsPanel(controller);

        configureFrame();
        arrangeComponents();
        connectViewActions();
        refreshView();
    }

    private void configureFrame() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setMinimumSize(new Dimension(1100, 680));
        setSize(1400, 800);
        setLocationRelativeTo(null);
    }

    private void arrangeComponents() {
        JScrollPane informationScrollPane =
                new JScrollPane(hexInfoPanel);

        informationScrollPane.setBorder(null);
        informationScrollPane.setPreferredSize(
                new Dimension(315, 0)
        );
        informationScrollPane.setHorizontalScrollBarPolicy(
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        );
        informationScrollPane.getVerticalScrollBar()
                .setUnitIncrement(14);

        add(hudPanel, BorderLayout.NORTH);
        add(technologyPanel, BorderLayout.WEST);
        add(hexMapPanel, BorderLayout.CENTER);
        add(informationScrollPane, BorderLayout.EAST);
        add(buildingActionsPanel, BorderLayout.SOUTH);
    }

    private void connectViewActions() {
        GameState gameState = controller.getGameState();

        hexMapPanel.setSelectionListener(
                (tile, unit) -> {
                    hexInfoPanel.showSelection(
                            tile,
                            unit,
                            gameState.getResourceBuildingAt(
                                    tile.getCoordinate()
                            ).orElse(null),
                            gameState.getSettlementAt(
                                    tile.getCoordinate()
                            ).orElse(null),
                            tile.getCoordinate().equals(
                                    gameState
                                            .getTownHallCoordinate()
                            )
                    );

                    buildingActionsPanel.refresh(unit);
                }
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
                        "Turn completed and economy updated"
                )
        );

        technologyPanel.setResearchAction(
                technologyType -> executeAction(
                        () -> controller
                                .startTechnologyResearch(
                                        technologyType
                                ),
                        technologyType.getDisplayName() +
                                " research started"
                )
        );

        buildingActionsPanel.setBuildAction(
                (builderId, buildingType) -> executeAction(
                        () -> controller
                                .constructResourceBuilding(
                                        builderId,
                                        buildingType
                                ),
                        buildingType.getDisplayName() +
                                " constructed"
                )
        );

        buildingActionsPanel.setSettlementAction(
                builderId -> executeAction(
                        () -> controller.constructSettlement(
                                builderId
                        ),
                        "Settlement constructed; unit cap increased"
                )
        );

        buildingActionsPanel.setStationAction(
                workerId -> executeAction(
                        () -> controller.stationWorker(workerId),
                        "Worker stationed"
                )
        );

        buildingActionsPanel.setUnstationAction(
                workerId -> executeAction(
                        () -> controller.unstationWorker(workerId),
                        "Worker left the building"
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
            hudPanel.setStatusMessage(successMessage, false);
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
        hudPanel.refresh(controller.getGameState());
        technologyPanel.refresh(
                controller.getGameState().getTownHall()
        );
        hexMapPanel.refreshFromModel();
    }
}
