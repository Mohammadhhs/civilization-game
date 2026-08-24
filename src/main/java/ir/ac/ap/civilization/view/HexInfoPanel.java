package ir.ac.ap.civilization.view;

import ir.ac.ap.civilization.model.map.HexTile;
import ir.ac.ap.civilization.model.map.NaturalResourceType;
import ir.ac.ap.civilization.model.map.TerrainType;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.util.Objects;
import java.util.Set;

public final class HexInfoPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private static final Color BACKGROUND_COLOR =
            new Color(30, 35, 43);

    private static final Color CARD_COLOR =
            new Color(45, 52, 62);

    private static final Color TEXT_COLOR =
            new Color(235, 239, 244);

    private static final Color ACCENT_COLOR =
            new Color(255, 214, 64);

    private final JLabel coordinateLabel;
    private final JLabel terrainLabel;
    private final JLabel resourcesLabel;
    private final JLabel movementLabel;
    private final JLabel passableLabel;
    private final JLabel constructionLabel;
    private final JLabel structureLabel;

    public HexInfoPanel() {
        setLayout(
                new BoxLayout(
                        this,
                        BoxLayout.Y_AXIS
                )
        );

        setBackground(BACKGROUND_COLOR);
        setPreferredSize(new Dimension(255, 0));

        setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(
                                0,
                                1,
                                0,
                                0,
                                new Color(55, 64, 75)
                        ),
                        new EmptyBorder(18, 14, 18, 14)
                )
        );

        JLabel titleLabel = new JLabel(
                "Selected Hex",
                SwingConstants.CENTER
        );

        titleLabel.setFont(
                new Font(
                        Font.SANS_SERIF,
                        Font.BOLD,
                        20
                )
        );

        titleLabel.setForeground(ACCENT_COLOR);
        titleLabel.setAlignmentX(0.5f);

        coordinateLabel = createInformationLabel();
        terrainLabel = createInformationLabel();
        resourcesLabel = createInformationLabel();
        movementLabel = createInformationLabel();
        passableLabel = createInformationLabel();
        constructionLabel = createInformationLabel();
        structureLabel = createInformationLabel();

        add(titleLabel);
        add(Box.createVerticalStrut(18));
        add(coordinateLabel);
        add(Box.createVerticalStrut(8));
        add(terrainLabel);
        add(Box.createVerticalStrut(8));
        add(resourcesLabel);
        add(Box.createVerticalStrut(8));
        add(movementLabel);
        add(Box.createVerticalStrut(8));
        add(passableLabel);
        add(Box.createVerticalStrut(8));
        add(constructionLabel);
        add(Box.createVerticalStrut(8));
        add(structureLabel);
        add(Box.createVerticalGlue());

        clearInformation();
    }

    public void showTile(
            HexTile tile,
            boolean containsTownHall
    ) {
        Objects.requireNonNull(
                tile,
                "Hex tile cannot be null"
        );

        coordinateLabel.setText(
                "Coordinate: (" +
                        tile.getCoordinate().getQ() +
                        ", " +
                        tile.getCoordinate().getR() +
                        ")"
        );

        terrainLabel.setText(
                "Terrain: " +
                        getTerrainDisplayName(
                                tile.getTerrainType()
                        )
        );

        resourcesLabel.setText(
                "Resources: " +
                        getResourcesDisplayText(
                                tile.getNaturalResources()
                        )
        );

        movementLabel.setText(
                tile.isNormallyPassable()
                        ? "Movement cost: " +
                        tile.getBaseMovementCost() +
                        " AP"
                        : "Movement cost: Blocked"
        );

        passableLabel.setText(
                "Normally passable: " +
                        getYesNoText(
                                tile.isNormallyPassable()
                        )
        );

        constructionLabel.setText(
                "Construction allowed: " +
                        getYesNoText(
                                tile.isConstructionAllowed()
                        )
        );

        structureLabel.setText(
                "Structure: " +
                        (
                                containsTownHall
                                        ? "Town Hall"
                                        : "None"
                        )
        );
    }

    public void clearInformation() {
        coordinateLabel.setText("Coordinate: -");
        terrainLabel.setText("Terrain: -");
        resourcesLabel.setText("Resources: -");
        movementLabel.setText("Movement cost: -");
        passableLabel.setText("Normally passable: -");
        constructionLabel.setText(
                "Construction allowed: -"
        );
        structureLabel.setText("Structure: -");
    }

    private JLabel createInformationLabel() {
        JLabel label = new JLabel();

        label.setOpaque(true);
        label.setBackground(CARD_COLOR);
        label.setForeground(TEXT_COLOR);

        label.setFont(
                new Font(
                        Font.SANS_SERIF,
                        Font.PLAIN,
                        13
                )
        );

        label.setHorizontalAlignment(
                SwingConstants.LEFT
        );

        label.setAlignmentX(0.5f);

        label.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                new Color(62, 71, 83)
                        ),
                        new EmptyBorder(10, 9, 10, 9)
                )
        );

        label.setMaximumSize(
                new Dimension(
                        Integer.MAX_VALUE,
                        42
                )
        );

        return label;
    }

    private String getTerrainDisplayName(
            TerrainType terrainType
    ) {
        switch (terrainType) {
            case FOREST:
                return "Forest";

            case PLAIN:
                return "Plain";

            case MOUNTAIN:
                return "Mountain";

            case GRASSLAND:
                return "Grassland";

            case SEA:
                return "Sea";

            case MOUNTAIN_RANGE:
                return "Mountain Range";

            default:
                throw new IllegalStateException(
                        "Unknown terrain type"
                );
        }
    }

    private String getResourcesDisplayText(
            Set<NaturalResourceType> resources
    ) {
        if (resources.isEmpty()) {
            return "None";
        }

        StringBuilder builder =
                new StringBuilder();

        for (NaturalResourceType resource : resources) {
            if (builder.length() > 0) {
                builder.append(", ");
            }

            builder.append(
                    getResourceDisplayName(resource)
            );
        }

        return builder.toString();
    }

    private String getResourceDisplayName(
            NaturalResourceType resource
    ) {
        switch (resource) {
            case WOOD:
                return "Wood";

            case STONE:
                return "Stone";

            case IRON:
                return "Iron";

            case WHEAT:
                return "Wheat";

            case RICE:
                return "Rice";

            case CATTLE:
                return "Cattle";

            case SHEEP:
                return "Sheep";

            case FISH:
                return "Fish";

            default:
                throw new IllegalStateException(
                        "Unknown natural resource"
                );
        }
    }

    private String getYesNoText(boolean value) {
        return value ? "Yes" : "No";
    }
}