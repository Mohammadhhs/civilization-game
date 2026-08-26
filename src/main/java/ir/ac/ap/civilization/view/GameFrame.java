package ir.ac.ap.civilization.view;

import ir.ac.ap.civilization.controller.GameController;
import ir.ac.ap.civilization.model.game.GameState;
import ir.ac.ap.civilization.model.game.GameSaveService;
import ir.ac.ap.civilization.model.game.SaveSlot;
import ir.ac.ap.civilization.view.asset.GameImageRepository;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import java.io.IOException;
import java.nio.file.Paths;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.Objects;

public final class GameFrame extends JFrame {

    private static final long serialVersionUID = 1L;

    private final GameController controller;
    private final GameImageRepository imageRepository;
    private final HudPanel hudPanel;
    private final HexMapPanel hexMapPanel;
    private final HexInfoPanel hexInfoPanel;
    private final TechnologyPanel technologyPanel;
    private final BuildingActionsPanel buildingActionsPanel;
    private final UnitProductionPanel unitProductionPanel;
    private final GameSaveService saveService;

    public GameFrame(GameController controller) {
        super("Civilization - Advanced Programming Project");

        this.controller = Objects.requireNonNull(
                controller,
                "Game controller cannot be null"
        );

        this.imageRepository = new GameImageRepository();
        this.hudPanel = new HudPanel(imageRepository);
        this.hexMapPanel = new HexMapPanel(controller, imageRepository);
        this.hexInfoPanel = new HexInfoPanel(imageRepository);
        this.technologyPanel = new TechnologyPanel();
        this.buildingActionsPanel =
                new BuildingActionsPanel(controller, imageRepository);
        this.unitProductionPanel =
                new UnitProductionPanel(controller, imageRepository);
        this.saveService = new GameSaveService(
                Paths.get(System.getProperty("user.home"),
                        ".civilization", "saves")
        );

        configureFrame();
        setJMenuBar(createMenuBar());
        arrangeComponents();
        connectViewActions();
        refreshView();
        showAssetStatus();
    }

    private void configureFrame() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setMinimumSize(new Dimension(1280, 800));
        setSize(1600, 1000);
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
    }

    private void arrangeComponents() {
        JScrollPane informationScrollPane =
                new JScrollPane(hexInfoPanel);

        informationScrollPane.setBorder(null);
        informationScrollPane.setPreferredSize(
                new Dimension(295, 0)
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
                            ),
                            gameState.getTownHall().getCurrentLevel()
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
                    try {
                        saveService.save(
                                controller.getGameState(),
                                SaveSlot.AUTOSAVE,
                                "Autosave"
                        );
                    } catch (IOException exception) {
                        hudPanel.setStatusMessage(
                                "Autosave failed: " + exception.getMessage(),
                                true
                        );
                    }
                    hexMapPanel.refreshFromModel();
                },
                "Turn completed and economy updated"
        );
    }

    private JMenuBar createMenuBar() {
        JMenuBar bar = new JMenuBar();
        JMenu game = new JMenu("Game");
        JMenu save = new JMenu("Save");
        JMenu load = new JMenu("Load");
        for (SaveSlot slot : SaveSlot.values()) {
            if (slot != SaveSlot.AUTOSAVE) {
                JMenuItem saveItem = new JMenuItem(slot.name());
                saveItem.addActionListener(event -> save(slot));
                save.add(saveItem);
            }
            JMenuItem loadItem = new JMenuItem(slot.name());
            loadItem.addActionListener(event -> load(slot));
            load.add(loadItem);
        }
        JMenuItem phaseTwo = new JMenuItem("Phase II Command Center");
        phaseTwo.addActionListener(event ->
                new PhaseTwoDialog(this, controller).setVisible(true));
        game.add(save);
        game.add(load);
        game.addSeparator();
        game.add(phaseTwo);
        bar.add(game);
        return bar;
    }

    private void save(SaveSlot slot) {
        try {
            saveService.save(controller.getGameState(), slot, slot.name());
            hudPanel.setStatusMessage("Saved to " + slot, false);
        } catch (IOException | RuntimeException exception) {
            JOptionPane.showMessageDialog(this, exception.getMessage(),
                    "Save failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void load(SaveSlot slot) {
        if (JOptionPane.showConfirmDialog(this,
                "Unsaved progress will be lost. Continue?", "Load game",
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        try {
            GameState loaded = saveService.load(slot);
            GameFrame frame = new GameFrame(new GameController(loaded));
            frame.setVisible(true);
            dispose();
        } catch (IOException exception) {
            JOptionPane.showMessageDialog(this, exception.getMessage(),
                    "Load failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshView() {
        hudPanel.refresh(controller.getGameState());
        technologyPanel.refresh(
                controller.getGameState().getTownHall()
        );
        unitProductionPanel.refresh();
        hexMapPanel.refreshFromModel();
    }

    private void showAssetStatus() {
        int missingCount = imageRepository
                .getMissingRequiredPaths()
                .size();

        if (missingCount == 0) {
            hudPanel.setStatusMessage(
                    imageRepository.getLoadedRequiredImageCount() +
                            " image assets loaded",
                    false
            );
            hudPanel.setToolTipText(
                    "All 27 required image assets were loaded"
            );
        } else {
            hudPanel.setStatusMessage(
                    missingCount +
                            " image asset(s) missing; vector fallback active",
                    true
            );
            hudPanel.setToolTipText(
                    "<html>Missing image assets:<br>" +
                            String.join(
                                    "<br>",
                                    imageRepository
                                            .getMissingRequiredPaths()
                            ) +
                            "</html>"
            );
        }
    }
}
