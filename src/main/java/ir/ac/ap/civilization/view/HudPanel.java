package ir.ac.ap.civilization.view;

import ir.ac.ap.civilization.model.building.TownHall;
import ir.ac.ap.civilization.model.building.TownHallLevel;
import ir.ac.ap.civilization.model.game.EconomyService;
import ir.ac.ap.civilization.model.game.EconomyStatus;
import ir.ac.ap.civilization.model.game.EconomyForecastService;
import ir.ac.ap.civilization.model.game.GameState;
import ir.ac.ap.civilization.model.game.ResourceFlowForecast;
import ir.ac.ap.civilization.model.resource.ResourceStockpile;
import ir.ac.ap.civilization.model.resource.ResourceType;
import ir.ac.ap.civilization.model.unit.UnitType;
import ir.ac.ap.civilization.view.asset.GameImageRepository;
import ir.ac.ap.civilization.view.asset.ImageScaler;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

public final class HudPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private static final Color BACKGROUND_COLOR =
            new Color(30, 35, 43);

    private static final Color CARD_COLOR =
            new Color(45, 52, 62);

    private static final Color TEXT_COLOR =
            new Color(235, 239, 244);

    private static final Color SECONDARY_TEXT_COLOR =
            new Color(174, 183, 194);

    private static final Color ACCENT_COLOR =
            new Color(74, 170, 108);

    private static final Color WARNING_COLOR =
            new Color(224, 161, 70);

    private static final Color ERROR_COLOR =
            new Color(224, 92, 92);

    private final JLabel turnLabel;
    private final JLabel unitCapLabel;
    private final JLabel unitBreakdownLabel;
    private final JLabel townHallLevelLabel;
    private final JLabel townHallHpLabel;
    private final JLabel queueLabel;
    private final JLabel economyLabel;
    private final JLabel crisisLabel;
    private final JLabel statusLabel;

    private final Map<ResourceType, JLabel> resourceLabels;
    private final GameImageRepository imageRepository;

    private final JButton startUpgradeButton;
    private final JButton cancelCommandButton;
    private final JButton endTurnButton;

    public HudPanel() {
        this(new GameImageRepository());
    }

    public HudPanel(GameImageRepository imageRepository) {
        this.imageRepository = Objects.requireNonNull(
                imageRepository,
                "Image repository cannot be null"
        );
        setLayout(new BorderLayout(8, 8));
        setBackground(BACKGROUND_COLOR);
        setBorder(new EmptyBorder(8, 8, 8, 8));
        setPreferredSize(new Dimension(0, 174));

        turnLabel = createInformationLabel("Turn: 1");

        unitCapLabel = createInformationLabel("Units: 5/8");

        unitBreakdownLabel = createInformationLabel(
                "B: 2 | W: 2 | E: 1 | BE: 0"
        );

        townHallLevelLabel =
                createInformationLabel("Town Hall: Base Camp");

        townHallHpLabel =
                createInformationLabel("HP: 200/200");

        queueLabel =
                createInformationLabel("Production: Idle");

        queueLabel.setForeground(WARNING_COLOR);

        economyLabel = createInformationLabel(
                "Food upkeep: 10/turn | Economy stable"
        );

        crisisLabel = new JLabel(
                "Economy stable",
                SwingConstants.CENTER
        );
        crisisLabel.setFont(
                new Font(Font.SANS_SERIF, Font.BOLD, 13)
        );
        crisisLabel.setForeground(ACCENT_COLOR);
        crisisLabel.setAlignmentX(0.5f);

        statusLabel = new JLabel(
                "Game ready",
                SwingConstants.CENTER
        );

        statusLabel.setFont(
                new Font(Font.SANS_SERIF, Font.PLAIN, 13)
        );

        statusLabel.setForeground(SECONDARY_TEXT_COLOR);
        statusLabel.setAlignmentX(0.5f);

        resourceLabels =
                new EnumMap<>(ResourceType.class);

        startUpgradeButton =
                createButton("Start Upgrade", ACCENT_COLOR);

        cancelCommandButton =
                createButton(
                        "Cancel Command",
                        WARNING_COLOR
                );

        endTurnButton =
                createButton("End Turn", ACCENT_COLOR);

        add(createInformationSection(), BorderLayout.CENTER);
        add(createControlSection(), BorderLayout.SOUTH);
    }

    public void setStartUpgradeAction(
            ActionListener listener
    ) {
        startUpgradeButton.addActionListener(
                Objects.requireNonNull(
                        listener,
                        "Start upgrade listener cannot be null"
                )
        );
    }

    public void setCancelCommandAction(
            ActionListener listener
    ) {
        cancelCommandButton.addActionListener(
                Objects.requireNonNull(
                        listener,
                        "Cancel command listener cannot be null"
                )
        );
    }

    public void setEndTurnAction(
            ActionListener listener
    ) {
        endTurnButton.addActionListener(
                Objects.requireNonNull(
                        listener,
                        "End turn listener cannot be null"
                )
        );
    }

    public void refresh(GameState gameState) {
        Objects.requireNonNull(
                gameState,
                "Game state cannot be null"
        );

        TownHall townHall =
                gameState.getTownHall();

        ResourceStockpile stockpile =
                townHall.getStockpile();

        turnLabel.setText(
                "Turn: " + gameState.getTurnNumber()
        );

        unitCapLabel.setText(
                "Units: " +
                        gameState.getActiveUnitCount() +
                        "/" +
                        gameState.getUnitCap()
        );

        unitCapLabel.setForeground(
                gameState.isOverUnitCap()
                        ? ERROR_COLOR
                        : TEXT_COLOR
        );

        unitBreakdownLabel.setText(
                "Builder: " +
                        gameState.getActiveUnitCount(
                                UnitType.BUILDER
                        ) +
                        " | Worker: " +
                        gameState.getActiveUnitCount(
                                UnitType.WORKER
                        ) +
                        " | Explorer: " +
                        gameState.getActiveUnitCount(
                                UnitType.EXPLORER
                        ) +
                        " | Border: " +
                        gameState.getActiveUnitCount(
                                UnitType.BORDER_EXPANDER
                        ) +
                        " | Military: " +
                        gameState.getActiveMilitaryUnitCount() +
                        "/" + gameState.getMilitaryUnitCap()
        );

        townHallLevelLabel.setText(
                "TH " +
                        townHall.getCurrentLevel()
                                .getDisplayName()
        );
        setLabelImage(
                townHallLevelLabel,
                imageRepository.getTownHallImage(
                        townHall.getCurrentLevel()
                ),
                30
        );

        townHallHpLabel.setText(
                "HP: " +
                        townHall.getCurrentHp() +
                        "/" +
                        townHall.getMaximumHp()
        );

        townHallLevelLabel.setText(
                "<html><center>TH " +
                        townHall.getCurrentLevel()
                                .getDisplayName() +
                        "<br>" +
                        townHall.getCurrentHp() +
                        "/" +
                        townHall.getMaximumHp() +
                        "</center></html>"
        );

        ResourceFlowForecast forecast =
                new EconomyForecastService(gameState)
                        .calculate();

        refreshResourceLabels(stockpile, forecast);

        refreshQueueInformation(townHall);
        refreshEconomyInformation(gameState);
        crisisLabel.setText(
                "Season: " + gameState.getPhaseTwoState().getSeason() +
                        " | Happiness: " +
                        gameState.getPhaseTwoState().getHappiness().getValue() +
                        " (" +
                        gameState.getPhaseTwoState().getHappiness().getLevel() +
                        ")"
        );
        refreshButtonStates(townHall);

        revalidate();
        repaint();
    }

    public void setStatusMessage(
            String message,
            boolean error
    ) {
        statusLabel.setText(
                Objects.requireNonNull(
                        message,
                        "Status message cannot be null"
                )
        );

        statusLabel.setForeground(
                error ? ERROR_COLOR : SECONDARY_TEXT_COLOR
        );
    }

    private JPanel createInformationSection() {
        JPanel informationPanel = new JPanel(
                new BorderLayout(6, 6)
        );
        informationPanel.setOpaque(false);

        JPanel summaryRow = new JPanel(
                new GridLayout(1, 8, 6, 0)
        );
        summaryRow.setOpaque(false);
        configureCompactSummaryLabel(turnLabel);
        summaryRow.add(turnLabel);

        for (ResourceType resourceType :
                ResourceType.values()) {

            JLabel resourceLabel =
                    createInformationLabel(
                            getResourceDisplayName(resourceType)
                    );

            setLabelImage(
                    resourceLabel,
                    imageRepository.getStockpileImage(resourceType),
                    24
            );
            configureCompactSummaryLabel(resourceLabel);

            resourceLabels.put(
                    resourceType,
                    resourceLabel
            );

            summaryRow.add(resourceLabel);
        }

        configureCompactSummaryLabel(unitCapLabel);
        configureCompactSummaryLabel(townHallLevelLabel);
        configureCompactSummaryLabel(queueLabel);
        summaryRow.add(unitCapLabel);
        summaryRow.add(townHallLevelLabel);
        summaryRow.add(queueLabel);

        JPanel detailRows = new JPanel(
                new GridLayout(2, 2, 6, 4)
        );
        detailRows.setOpaque(false);
        detailRows.add(unitBreakdownLabel);
        detailRows.add(economyLabel);
        detailRows.add(crisisLabel);
        detailRows.add(statusLabel);

        informationPanel.add(summaryRow, BorderLayout.CENTER);
        informationPanel.add(detailRows, BorderLayout.SOUTH);

        return informationPanel;
    }

    private JPanel createControlSection() {
        JPanel controlPanel =
                new JPanel(new FlowLayout(
                        FlowLayout.CENTER,
                        12,
                        0
                ));

        controlPanel.setOpaque(false);

        controlPanel.add(startUpgradeButton);
        controlPanel.add(cancelCommandButton);
        controlPanel.add(endTurnButton);

        return controlPanel;
    }

    private JLabel createInformationLabel(String text) {
        JLabel label = new JLabel(
                text,
                SwingConstants.CENTER
        );

        label.setOpaque(true);
        label.setBackground(CARD_COLOR);
        label.setForeground(TEXT_COLOR);

        label.setFont(
                new Font(Font.SANS_SERIF, Font.BOLD, 14)
        );

        label.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                new Color(62, 71, 83)
                        ),
                        new EmptyBorder(8, 8, 8, 8)
                )
        );

        return label;
    }

    private void configureCompactSummaryLabel(JLabel label) {
        label.setFont(
                new Font(Font.SANS_SERIF, Font.BOLD, 12)
        );
        label.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                new Color(62, 71, 83)
                        ),
                        new EmptyBorder(5, 4, 5, 4)
                )
        );
    }

    private JButton createButton(
            String text,
            Color backgroundColor
    ) {
        JButton button = new JButton(text);

        button.setFont(
                new Font(Font.SANS_SERIF, Font.BOLD, 14)
        );

        button.setForeground(Color.BLACK);
        button.setBackground(backgroundColor);
        button.setFocusPainted(false);

        button.setCursor(
                Cursor.getPredefinedCursor(
                        Cursor.HAND_CURSOR
                )
        );

        button.setPreferredSize(
                new Dimension(170, 36)
        );

        return button;
    }

    private void setLabelImage(
            JLabel label,
            BufferedImage image,
            int size
    ) {
        BufferedImage scaled = ImageScaler.fit(image, size, size);
        label.setIcon(
                scaled == null ? null : new ImageIcon(scaled)
        );
        label.setIconTextGap(8);
    }

    private void refreshResourceLabels(
            ResourceStockpile stockpile,
            ResourceFlowForecast forecast
    ) {
        int capacity =
                stockpile.getCapacityPerResource();

        for (ResourceType resourceType :
                ResourceType.values()) {

            int amount =
                    stockpile.getAmount(resourceType);

            int production =
                    forecast.getNetChange(resourceType);

            String productionPrefix =
                    production >= 0 ? "+" : "";

            JLabel label =
                    resourceLabels.get(resourceType);

            label.setText(
                    getResourceDisplayName(resourceType) +
                            " " +
                            amount +
                            "/" +
                            capacity +
                            " | " +
                            productionPrefix +
                            production +
                            "/t"
            );
            label.setToolTipText(
                    getResourceDisplayName(resourceType) +
                            ": " + amount + "/" + capacity +
                            ", net " + productionPrefix +
                            production + " per turn"
            );

            label.setForeground(
                    production < 0
                            ? ERROR_COLOR
                            : TEXT_COLOR
            );
        }
    }

    private void refreshQueueInformation(
            TownHall townHall
    ) {
        if (!townHall.isBusy()) {
            queueLabel.setText("Command: Idle");
            queueLabel.setToolTipText(
                    "Town Hall production queue is idle"
            );
            return;
        }

        queueLabel.setText(
                "Command: " +
                        townHall.getActiveCommandName() +
                        " (" +
                        townHall.getRemainingProductionTurns() +
                        "T)"
        );
        queueLabel.setToolTipText(
                townHall.getActiveCommandName() +
                        ": " +
                        townHall.getRemainingProductionTurns() +
                        " turn(s) remaining"
        );
    }

    private void refreshEconomyInformation(
            GameState gameState
    ) {
        EconomyStatus economyStatus =
                gameState.getEconomyStatus();

        int nextFoodUpkeep = Math.multiplyExact(
                gameState.getUnits().size(),
                EconomyService.FOOD_UPKEEP_PER_UNIT
        );

        economyLabel.setText(
                "Food upkeep: " +
                        nextFoodUpkeep +
                        "/turn | Last paid: " +
                        economyStatus.getPaidUnitFood() +
                        "/" +
                        economyStatus.getRequiredUnitFood() +
                        " | Buildings at risk: " +
                        economyStatus.getBuildingsAtRisk()
        );

        if (!economyStatus.hasResourceCrisis()) {
            crisisLabel.setText("Economy stable");
            crisisLabel.setForeground(ACCENT_COLOR);
            return;
        }

        StringBuilder crisis = new StringBuilder("CRISIS: ");

        if (economyStatus.isStarving()) {
            crisis.append("Starvation - Food deficit ")
                    .append(economyStatus.getFoodDeficit())
                    .append(" (-")
                    .append(
                            EconomyService
                                    .STARVATION_ACTION_POINT_PENALTY
                    )
                    .append(" AP)");
        }

        if (economyStatus.getBuildingsAtRisk() > 0) {
            if (economyStatus.isStarving()) {
                crisis.append(" | ");
            }

            crisis.append(
                    economyStatus.getBuildingsAtRisk()
            ).append(" building(s) missing upkeep");
        }

        if (economyStatus
                .getBuildingsDestroyedLastTurn() > 0) {
            if (crisis.length() > "CRISIS: ".length()) {
                crisis.append(" | ");
            }

            crisis.append(
                    economyStatus
                            .getBuildingsDestroyedLastTurn()
            ).append(" building(s) destroyed");
        }

        crisisLabel.setText(crisis.toString());
        crisisLabel.setForeground(ERROR_COLOR);
    }

    private void refreshButtonStates(
            TownHall townHall
    ) {
        startUpgradeButton.setEnabled(
                townHall.canStartUpgrade()
        );

        cancelCommandButton.setEnabled(
                townHall.isBusy()
        );

        TownHallLevel currentLevel =
                townHall.getCurrentLevel();

        if (currentLevel.isMaximumLevel()) {
            startUpgradeButton.setText(
                    "Maximum Level"
            );

            startUpgradeButton.setToolTipText(
                    "Town Hall has reached its maximum level"
            );

            return;
        }

        TownHallLevel nextLevel =
                currentLevel.getNextLevel();

        startUpgradeButton.setText(
                "Upgrade to " +
                        nextLevel.getDisplayName()
        );

        if (townHall.canStartUpgrade()) {
            startUpgradeButton.setToolTipText(
                    "Start the Town Hall upgrade"
            );
        } else if (townHall.isBusy()) {
            startUpgradeButton.setToolTipText(
                    "Town Hall is currently busy"
            );
        } else {
            startUpgradeButton.setToolTipText(
                    "Not enough resources for this upgrade"
            );
        }
    }

    private String getResourceDisplayName(
            ResourceType resourceType
    ) {
        switch (resourceType) {
            case FOOD:
                return "Food";

            case WOOD:
                return "Wood";

            case STONE:
                return "Stone";

            case IRON:
                return "Iron";

            default:
                throw new IllegalStateException(
                        "Unknown resource type"
                );
        }
    }
}
