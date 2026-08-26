package ir.ac.ap.civilization.view;

import ir.ac.ap.civilization.controller.GameController;
import ir.ac.ap.civilization.model.building.BuildingType;
import ir.ac.ap.civilization.model.resource.ResourceType;
import ir.ac.ap.civilization.model.unit.Builder;
import ir.ac.ap.civilization.model.unit.BorderExpander;
import ir.ac.ap.civilization.model.unit.Unit;
import ir.ac.ap.civilization.model.unit.Worker;
import ir.ac.ap.civilization.view.asset.GameImageRepository;
import ir.ac.ap.civilization.view.asset.ImageComboBoxRenderer;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.Color;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.IntConsumer;

public final class BuildingActionsPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private static final Color BACKGROUND_COLOR =
            new Color(30, 35, 43);
    private static final Color TEXT_COLOR =
            new Color(235, 239, 244);
    private static final Color BUILD_COLOR =
            new Color(205, 125, 55);
    private static final Color WORKER_COLOR =
            new Color(70, 145, 205);
    private static final Color SETTLEMENT_COLOR =
            new Color(143, 92, 184);
    private static final Color BORDER_COLOR =
            new Color(227, 176, 64);

    private final GameController controller;
    private final JComboBox<BuildingType> buildingTypeBox;
    private final JLabel costLabel;
    private final JButton buildButton;
    private final JButton settlementButton;
    private final JButton expandBorderButton;
    private final JButton stationButton;
    private final JButton unstationButton;

    private Unit selectedUnit;
    private BiConsumer<Integer, BuildingType> buildAction;
    private IntConsumer settlementAction;
    private IntConsumer expandBorderAction;
    private IntConsumer stationAction;
    private IntConsumer unstationAction;

    public BuildingActionsPanel(GameController controller) {
        this(controller, new GameImageRepository());
    }

    public BuildingActionsPanel(
            GameController controller,
            GameImageRepository imageRepository
    ) {
        Objects.requireNonNull(
                imageRepository,
                "Image repository cannot be null"
        );
        this.controller = Objects.requireNonNull(
                controller,
                "Game controller cannot be null"
        );

        setLayout(new BorderLayout(8, 7));
        setBackground(BACKGROUND_COLOR);
        setPreferredSize(new Dimension(0, 104));
        setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(
                                1, 0, 0, 0,
                                new Color(55, 64, 75)
                        ),
                        new EmptyBorder(3, 8, 3, 8)
                )
        );

        buildAction = (unitId, type) -> {
        };
        settlementAction = unitId -> {
        };
        expandBorderAction = unitId -> {
        };
        stationAction = unitId -> {
        };
        unstationAction = unitId -> {
        };

        JLabel titleLabel = new JLabel("Building actions:");
        titleLabel.setForeground(TEXT_COLOR);
        titleLabel.setFont(
                new Font(Font.SANS_SERIF, Font.BOLD, 14)
        );

        buildingTypeBox = new JComboBox<>(
                BuildingType.values()
        );
        buildingTypeBox.setRenderer(
                new ImageComboBoxRenderer<BuildingType>(
                        imageRepository::getBuildingImage
                )
        );
        buildingTypeBox.setPreferredSize(
                new Dimension(170, 34)
        );

        costLabel = new JLabel();
        costLabel.setForeground(TEXT_COLOR);
        costLabel.setPreferredSize(new Dimension(250, 34));

        buildButton = createButton(
                "Build",
                BUILD_COLOR
        );
        settlementButton = createButton(
                "Build Settlement",
                SETTLEMENT_COLOR
        );
        settlementButton.setPreferredSize(
                new Dimension(170, 36)
        );
        expandBorderButton = createButton(
                "Expand Border",
                BORDER_COLOR
        );
        expandBorderButton.setPreferredSize(
                new Dimension(160, 36)
        );
        stationButton = createButton(
                "Station Worker",
                WORKER_COLOR
        );
        unstationButton = createButton(
                "Leave Building",
                WORKER_COLOR
        );

        buildingTypeBox.addActionListener(
                event -> {
                    refreshCostLabel();
                    refreshButtonStates();
                }
        );

        buildButton.addActionListener(
                event -> {
                    if (selectedUnit != null) {
                        buildAction.accept(
                                selectedUnit.getId(),
                                getSelectedBuildingType()
                        );
                    }
                }
        );

        settlementButton.addActionListener(
                event -> {
                    if (selectedUnit != null) {
                        settlementAction.accept(
                                selectedUnit.getId()
                        );
                    }
                }
        );

        expandBorderButton.addActionListener(
                event -> {
                    if (selectedUnit != null) {
                        expandBorderAction.accept(
                                selectedUnit.getId()
                        );
                    }
                }
        );

        stationButton.addActionListener(
                event -> {
                    if (selectedUnit != null) {
                        stationAction.accept(
                                selectedUnit.getId()
                        );
                    }
                }
        );

        unstationButton.addActionListener(
                event -> {
                    if (selectedUnit != null) {
                        unstationAction.accept(
                                selectedUnit.getId()
                        );
                    }
                }
        );

        JPanel selectorRow = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 10, 0)
        );
        selectorRow.setOpaque(false);
        selectorRow.add(titleLabel);
        selectorRow.add(buildingTypeBox);
        selectorRow.add(costLabel);

        JPanel actionRow = new JPanel(
                new GridLayout(1, 5, 8, 0)
        );
        actionRow.setOpaque(false);
        actionRow.add(buildButton);
        actionRow.add(settlementButton);
        actionRow.add(expandBorderButton);
        actionRow.add(stationButton);
        actionRow.add(unstationButton);

        add(selectorRow, BorderLayout.NORTH);
        add(actionRow, BorderLayout.CENTER);

        refreshCostLabel();
        refresh(null);
    }

    public void setBuildAction(
            BiConsumer<Integer, BuildingType> buildAction
    ) {
        this.buildAction = Objects.requireNonNull(
                buildAction,
                "Build action cannot be null"
        );
    }

    public void setStationAction(
            IntConsumer stationAction
    ) {
        this.stationAction = Objects.requireNonNull(
                stationAction,
                "Station action cannot be null"
        );
    }

    public void setSettlementAction(
            IntConsumer settlementAction
    ) {
        this.settlementAction = Objects.requireNonNull(
                settlementAction,
                "Settlement action cannot be null"
        );
    }

    public void setExpandBorderAction(
            IntConsumer expandBorderAction
    ) {
        this.expandBorderAction = Objects.requireNonNull(
                expandBorderAction,
                "Expand border action cannot be null"
        );
    }

    public void setUnstationAction(
            IntConsumer unstationAction
    ) {
        this.unstationAction = Objects.requireNonNull(
                unstationAction,
                "Unstation action cannot be null"
        );
    }

    public void refresh(Unit selectedUnit) {
        this.selectedUnit = selectedUnit;
        refreshButtonStates();
    }

    private void refreshButtonStates() {
        BuildingType buildingType =
                getSelectedBuildingType();

        boolean builderSelected =
                selectedUnit instanceof Builder;

        boolean workerSelected =
                selectedUnit instanceof Worker;

        boolean borderExpanderSelected =
                selectedUnit instanceof BorderExpander;

        buildButton.setEnabled(
                builderSelected &&
                        controller
                                .canConstructResourceBuilding(
                                        selectedUnit.getId(),
                                        buildingType
                                )
        );

        settlementButton.setEnabled(
                builderSelected &&
                        controller.canConstructSettlement(
                                selectedUnit.getId()
                        )
        );

        String borderExpansionProblem =
                borderExpanderSelected
                        ? controller.getBorderExpansionProblem(
                                selectedUnit.getId()
                        )
                        : "Select a Border Expander";

        expandBorderButton.setEnabled(
                borderExpanderSelected &&
                        borderExpansionProblem == null
        );

        stationButton.setEnabled(
                workerSelected &&
                        controller.canStationWorker(
                                selectedUnit.getId()
                        )
        );

        unstationButton.setEnabled(
                workerSelected &&
                        controller.canUnstationWorker(
                                selectedUnit.getId()
                        )
        );

        buildButton.setToolTipText(
                builderSelected
                        ? "Build instantly using AP, resources and one charge"
                        : "Select a Builder"
        );

        settlementButton.setToolTipText(
                builderSelected
                        ? "Requires Settlement Construction, " +
                        "Wood 60, Stone 50, Iron 20, 3 AP " +
                        "and one charge"
                        : "Select a Builder"
        );

        expandBorderButton.setToolTipText(
                borderExpansionProblem == null
                        ? "Claim this explored hex and its six neighbors"
                        : borderExpansionProblem
        );

        stationButton.setToolTipText(
                workerSelected
                        ? "Station the Worker for 1 AP"
                        : "Select a Worker"
        );

        unstationButton.setToolTipText(
                workerSelected
                        ? "Leave without refunding station AP"
                        : "Select a stationed Worker"
        );
    }

    private void refreshCostLabel() {
        BuildingType buildingType =
                getSelectedBuildingType();

        StringBuilder text = new StringBuilder("Cost: ");

        for (ResourceType resourceType :
                ResourceType.values()) {
            Integer amount = buildingType
                    .getConstructionCost()
                    .get(resourceType);

            if (amount == null || amount == 0) {
                continue;
            }

            if (text.length() > 6) {
                text.append(", ");
            }

            text.append(capitalize(resourceType.name()))
                    .append(' ')
                    .append(amount);
        }

        text.append(" | AP ")
                .append(
                        buildingType
                                .getConstructionActionPointCost()
                );

        costLabel.setText(text.toString());
    }

    private BuildingType getSelectedBuildingType() {
        return (BuildingType) Objects.requireNonNull(
                buildingTypeBox.getSelectedItem(),
                "Selected building type cannot be null"
        );
    }

    private JButton createButton(
            String text,
            Color backgroundColor
    ) {
        JButton button = new JButton(text);
        button.setPreferredSize(new Dimension(145, 36));
        button.setFocusPainted(false);
        button.setForeground(Color.BLACK);
        button.setBackground(backgroundColor);
        button.setFont(
                new Font(Font.SANS_SERIF, Font.BOLD, 12)
        );
        return button;
    }

    private String capitalize(String value) {
        String lowerCaseValue = value.toLowerCase();
        return Character.toUpperCase(
                lowerCaseValue.charAt(0)
        ) + lowerCaseValue.substring(1);
    }
}
