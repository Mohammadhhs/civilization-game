package ir.ac.ap.civilization.view;

import ir.ac.ap.civilization.controller.GameController;
import ir.ac.ap.civilization.model.building.BuildingType;
import ir.ac.ap.civilization.model.resource.ResourceType;
import ir.ac.ap.civilization.model.unit.Builder;
import ir.ac.ap.civilization.model.unit.Unit;
import ir.ac.ap.civilization.model.unit.Worker;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
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

    private final GameController controller;
    private final JComboBox<BuildingType> buildingTypeBox;
    private final JLabel costLabel;
    private final JButton buildButton;
    private final JButton stationButton;
    private final JButton unstationButton;

    private Unit selectedUnit;
    private BiConsumer<Integer, BuildingType> buildAction;
    private IntConsumer stationAction;
    private IntConsumer unstationAction;

    public BuildingActionsPanel(GameController controller) {
        this.controller = Objects.requireNonNull(
                controller,
                "Game controller cannot be null"
        );

        setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));
        setBackground(BACKGROUND_COLOR);
        setPreferredSize(new Dimension(0, 82));
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

        add(titleLabel);
        add(buildingTypeBox);
        add(costLabel);
        add(buildButton);
        add(stationButton);
        add(unstationButton);

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

        buildButton.setEnabled(
                builderSelected &&
                        controller
                                .canConstructResourceBuilding(
                                        selectedUnit.getId(),
                                        buildingType
                                )
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
        button.setForeground(Color.WHITE);
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
