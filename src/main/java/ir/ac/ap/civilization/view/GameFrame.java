package ir.ac.ap.civilization.view;

import ir.ac.ap.civilization.controller.GameController;
import ir.ac.ap.civilization.model.game.GameState;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
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
    private final UnitProductionPanel unitProductionPanel;

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
        this.unitProductionPanel =
                new UnitProductionPanel(controller);

        configureFrame();
        arrangeComponents();
        connectViewActions();
        refreshView();
    }

    private void configureFrame() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setMinimumSize(new Dimension(1280, 720));
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
        JPanel westPanel = new JPanel(new BorderLayout());
        westPanel.add(technologyPanel, BorderLayout.CENTER);
        westPanel.add(
                unitProductionPanel,
                BorderLayout.SOUTH
        );

        add(westPanel, BorderLayout.WEST);
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
                event -> requestEndTurn()
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

        buildingActionsPanel.setExpandBorderAction(
                unitId -> executeAction(
                        () -> controller.expandBorder(unitId),
                        "Border expanded and unit consumed"
                )
        );

        unitProductionPanel.setTrainAction(
                unitType -> executeAction(
                        () -> controller.startUnitProduction(
                                unitType
                        ),
                        unitType.getDisplayName() +
                                " production started"
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

    private void requestEndTurn() {
        int idleUnitCount = controller.getIdleUnitCount();

        if (idleUnitCount > 0) {
            int answer = JOptionPane.showConfirmDialog(
                    this,
                    idleUnitCount +
                            " unit(s) still have AP. End turn anyway?",
                    "Idle units",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );

            if (answer != JOptionPane.YES_OPTION) {
                return;
            }
        }

        executeAction(
                () -> {
                    controller.endTurn();
                    hexMapPanel.refreshFromModel();
                },
                "Turn completed and economy updated"
        );
    }

    private void refreshView() {
        hudPanel.refresh(controller.getGameState());
        technologyPanel.refresh(
                controller.getGameState().getTownHall()
        );
        unitProductionPanel.refresh();
        hexMapPanel.refreshFromModel();
    }
}
