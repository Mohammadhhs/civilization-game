package ir.ac.ap.civilization.view;

import ir.ac.ap.civilization.controller.GameController;
import ir.ac.ap.civilization.model.game.GameState;
import ir.ac.ap.civilization.model.resource.ResourceType;
import ir.ac.ap.civilization.model.unit.UnitType;
import ir.ac.ap.civilization.view.asset.GameImageRepository;
import ir.ac.ap.civilization.view.asset.ImageComboBoxRenderer;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.Objects;
import java.util.function.Consumer;

public final class UnitProductionPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private static final Color BACKGROUND_COLOR =
            new Color(30, 35, 43);
    private static final Color TEXT_COLOR =
            new Color(235, 239, 244);
    private static final Color ACCENT_COLOR =
            new Color(151, 82, 184);

    private final GameController controller;
    private final JComboBox<UnitType> unitTypeBox;
    private final JLabel costLabel;
    private final JButton trainButton;

    private Consumer<UnitType> trainAction;

    public UnitProductionPanel(GameController controller) {
        this(controller, new GameImageRepository());
    }

    public UnitProductionPanel(
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
        this.trainAction = unitType -> {
        };

        setLayout(new BorderLayout(6, 6));
        setBackground(BACKGROUND_COLOR);
        setPreferredSize(new Dimension(280, 176));
        setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(
                                1, 0, 0, 0,
                                new Color(55, 64, 75)
                        ),
                        new EmptyBorder(8, 10, 10, 10)
                )
        );

        JLabel title = new JLabel("Town Hall Units");
        title.setForeground(TEXT_COLOR);
        title.setFont(
                new Font(Font.SANS_SERIF, Font.BOLD, 16)
        );

        unitTypeBox = new JComboBox<>(UnitType.values());
        unitTypeBox.setRenderer(
                new ImageComboBoxRenderer<UnitType>(
                        imageRepository::getUnitImage
                )
        );
        unitTypeBox.setMaximumRowCount(UnitType.values().length);
        costLabel = new JLabel();
        costLabel.setForeground(TEXT_COLOR);
        costLabel.setFont(
                new Font(Font.SANS_SERIF, Font.PLAIN, 12)
        );

        trainButton = new JButton("Start Production");
        trainButton.setForeground(Color.BLACK);
        trainButton.setBackground(ACCENT_COLOR);
        trainButton.setFocusPainted(false);

        JPanel controls = new JPanel(new GridLayout(3, 1, 5, 5));
        controls.setOpaque(false);
        controls.add(unitTypeBox);
        controls.add(costLabel);
        controls.add(trainButton);

        add(title, BorderLayout.NORTH);
        add(controls, BorderLayout.CENTER);

        unitTypeBox.addActionListener(event -> refresh());
        trainButton.addActionListener(
                event -> trainAction.accept(getSelectedUnitType())
        );

        refresh();
    }

    public void setTrainAction(
            Consumer<UnitType> trainAction
    ) {
        this.trainAction = Objects.requireNonNull(
                trainAction,
                "Train action cannot be null"
        );
    }

    public void refresh() {
        UnitType unitType = getSelectedUnitType();
        GameState gameState = controller.getGameState();

        costLabel.setText(
                formatCost(unitType) +
                        " | " +
                        unitType.getProductionTurns() +
                        " turn(s)"
        );

        trainButton.setEnabled(
                controller.canStartUnitProduction(unitType)
        );

        if (!gameState.canCreateUnit()) {
            trainButton.setToolTipText("Unit cap reached");
        } else if (gameState.getTownHall().isBusy()) {
            trainButton.setToolTipText("Town Hall is busy");
        } else {
            trainButton.setToolTipText(
                    "Cost is paid when production starts"
            );
        }
    }

    private UnitType getSelectedUnitType() {
        return (UnitType) Objects.requireNonNull(
                unitTypeBox.getSelectedItem(),
                "Selected unit type cannot be null"
        );
    }

    private String formatCost(UnitType unitType) {
        StringBuilder result = new StringBuilder("Cost: ");

        for (ResourceType resourceType :
                ResourceType.values()) {
            Integer amount = unitType.getProductionCost()
                    .get(resourceType);

            if (amount == null) {
                continue;
            }

            if (result.length() > 6) {
                result.append(", ");
            }

            result.append(resourceType.name())
                    .append(' ')
                    .append(amount);
        }

        return result.toString();
    }
}
