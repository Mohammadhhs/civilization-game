package ir.ac.ap.civilization.view;

import ir.ac.ap.civilization.model.building.TownHall;
import ir.ac.ap.civilization.model.resource.ResourceType;
import ir.ac.ap.civilization.model.technology.TechnologyType;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

public final class TechnologyPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private static final Color BACKGROUND_COLOR =
            new Color(30, 35, 43);

    private static final Color CARD_COLOR =
            new Color(45, 52, 62);

    private static final Color TEXT_COLOR =
            new Color(235, 239, 244);

    private static final Color READY_COLOR =
            new Color(74, 170, 108);

    private static final Color LOCKED_COLOR =
            new Color(174, 183, 194);

    private static final Color ACTIVE_COLOR =
            new Color(224, 161, 70);

    private final Map<TechnologyType, JButton>
            researchButtons;

    private final Map<TechnologyType, JLabel>
            statusLabels;

    private Consumer<TechnologyType> researchAction;

    public TechnologyPanel() {
        setLayout(new BorderLayout());
        setBackground(BACKGROUND_COLOR);
        setBorder(new EmptyBorder(10, 10, 10, 10));
        setPreferredSize(new Dimension(290, 0));

        researchButtons =
                new EnumMap<>(TechnologyType.class);

        statusLabels =
                new EnumMap<>(TechnologyType.class);

        researchAction = technologyType -> {
        };

        add(createTitle(), BorderLayout.NORTH);
        add(createTechnologyList(), BorderLayout.CENTER);
    }

    public void setResearchAction(
            Consumer<TechnologyType> researchAction
    ) {
        this.researchAction = Objects.requireNonNull(
                researchAction,
                "Research action cannot be null"
        );
    }

    public void refresh(TownHall townHall) {
        Objects.requireNonNull(
                townHall,
                "Town Hall cannot be null"
        );

        for (TechnologyType technologyType :
                TechnologyType.values()) {
            refreshTechnology(
                    townHall,
                    technologyType
            );
        }

        revalidate();
        repaint();
    }

    private JLabel createTitle() {
        JLabel title = new JLabel(
                "Technologies",
                SwingConstants.CENTER
        );

        title.setForeground(TEXT_COLOR);
        title.setFont(
                new Font(Font.SANS_SERIF, Font.BOLD, 18)
        );
        title.setBorder(new EmptyBorder(4, 4, 12, 4));

        return title;
    }

    private JScrollPane createTechnologyList() {
        JPanel listPanel = new JPanel();
        listPanel.setLayout(
                new BoxLayout(listPanel, BoxLayout.Y_AXIS)
        );
        listPanel.setBackground(BACKGROUND_COLOR);

        for (TechnologyType technologyType :
                TechnologyType.values()) {
            listPanel.add(createTechnologyCard(technologyType));
            listPanel.add(Box.createVerticalStrut(8));
        }

        JScrollPane scrollPane = new JScrollPane(listPanel);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(BACKGROUND_COLOR);
        scrollPane.getVerticalScrollBar().setUnitIncrement(14);
        scrollPane.setHorizontalScrollBarPolicy(
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        );

        return scrollPane;
    }

    private JPanel createTechnologyCard(
            TechnologyType technologyType
    ) {
        JPanel card = new JPanel();
        card.setLayout(
                new BoxLayout(card, BoxLayout.Y_AXIS)
        );
        card.setBackground(CARD_COLOR);
        card.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                new Color(62, 71, 83)
                        ),
                        new EmptyBorder(9, 9, 9, 9)
                )
        );
        card.setAlignmentX(LEFT_ALIGNMENT);
        card.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 125)
        );

        JButton researchButton = new JButton(
                technologyType.getDisplayName()
        );
        researchButton.setForeground(Color.BLACK);
        researchButton.setAlignmentX(LEFT_ALIGNMENT);
        researchButton.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 32)
        );
        researchButton.setFocusPainted(false);
        researchButton.addActionListener(
                event -> researchAction.accept(technologyType)
        );

        JLabel detailsLabel = new JLabel(
                "<html>" +
                        technologyType.getResearchTurns() +
                        " turn(s) | " +
                        formatCost(technologyType) +
                        "</html>"
        );
        detailsLabel.setForeground(TEXT_COLOR);
        detailsLabel.setFont(
                new Font(Font.SANS_SERIF, Font.PLAIN, 12)
        );
        detailsLabel.setAlignmentX(LEFT_ALIGNMENT);

        JLabel statusLabel = new JLabel("Locked");
        statusLabel.setForeground(LOCKED_COLOR);
        statusLabel.setFont(
                new Font(Font.SANS_SERIF, Font.BOLD, 12)
        );
        statusLabel.setAlignmentX(LEFT_ALIGNMENT);

        researchButtons.put(technologyType, researchButton);
        statusLabels.put(technologyType, statusLabel);

        card.add(researchButton);
        card.add(Box.createVerticalStrut(7));
        card.add(detailsLabel);
        card.add(Box.createVerticalStrut(5));
        card.add(statusLabel);

        return card;
    }

    private void refreshTechnology(
            TownHall townHall,
            TechnologyType technologyType
    ) {
        JButton button = researchButtons.get(technologyType);
        JLabel statusLabel = statusLabels.get(technologyType);

        button.setEnabled(
                townHall.canStartResearch(technologyType)
        );

        if (townHall.hasTechnology(technologyType)) {
            statusLabel.setText("Completed");
            statusLabel.setForeground(READY_COLOR);
            button.setToolTipText("Research already completed");
            return;
        }

        if (townHall.getActiveResearchTechnology()
                .filter(technologyType::equals)
                .isPresent()) {
            statusLabel.setText(
                    "In progress: " +
                            townHall.getRemainingProductionTurns() +
                            " turn(s)"
            );
            statusLabel.setForeground(ACTIVE_COLOR);
            button.setToolTipText("Research is in progress");
            return;
        }

        if (!townHall.getTechnologyManager()
                .arePrerequisitesCompleted(technologyType)) {
            statusLabel.setText(
                    "Locked: " +
                            formatPrerequisites(technologyType)
            );
            statusLabel.setForeground(LOCKED_COLOR);
            button.setToolTipText(
                    "Complete the required technologies first"
            );
            return;
        }

        if (townHall.isBusy()) {
            statusLabel.setText("Town Hall is busy");
            statusLabel.setForeground(ACTIVE_COLOR);
            button.setToolTipText(
                    "Only one Town Hall command can be active"
            );
            return;
        }

        if (!townHall.getStockpile().canAfford(
                technologyType.getResearchCost()
        )) {
            statusLabel.setText("Not enough resources");
            statusLabel.setForeground(LOCKED_COLOR);
            button.setToolTipText(
                    "The research cost is not available"
            );
            return;
        }

        statusLabel.setText("Ready");
        statusLabel.setForeground(READY_COLOR);
        button.setToolTipText("Start technology research");
    }

    private String formatCost(
            TechnologyType technologyType
    ) {
        StringBuilder result = new StringBuilder();

        for (ResourceType resourceType :
                ResourceType.values()) {
            Integer amount = technologyType
                    .getResearchCost()
                    .get(resourceType);

            if (amount == null || amount == 0) {
                continue;
            }

            if (result.length() > 0) {
                result.append(", ");
            }

            result.append(getResourceName(resourceType))
                    .append(": ")
                    .append(amount);
        }

        return result.length() == 0
                ? "Free"
                : result.toString();
    }

    private String formatPrerequisites(
            TechnologyType technologyType
    ) {
        StringBuilder result = new StringBuilder();

        for (TechnologyType prerequisite :
                technologyType.getPrerequisites()) {
            if (result.length() > 0) {
                result.append(", ");
            }

            result.append(prerequisite.getDisplayName());
        }

        return result.toString();
    }

    private String getResourceName(
            ResourceType resourceType
    ) {
        String lowerCaseName =
                resourceType.name().toLowerCase();

        return Character.toUpperCase(
                lowerCaseName.charAt(0)
        ) + lowerCaseName.substring(1);
    }
}
