package ir.ac.ap.civilization.view;

import ir.ac.ap.civilization.model.building.ResourceBuilding;
import ir.ac.ap.civilization.model.building.Settlement;
import ir.ac.ap.civilization.model.building.TownHallLevel;
import ir.ac.ap.civilization.model.map.HexTile;
import ir.ac.ap.civilization.model.map.NaturalResourceType;
import ir.ac.ap.civilization.model.map.TerrainType;
import ir.ac.ap.civilization.model.unit.Builder;
import ir.ac.ap.civilization.model.unit.Unit;
import ir.ac.ap.civilization.model.unit.Worker;
import ir.ac.ap.civilization.view.asset.GameImageRepository;
import ir.ac.ap.civilization.view.asset.ImageScaler;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.util.Objects;

public final class HexInfoPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private static final Color BACKGROUND_COLOR =
            new Color(30, 35, 43);
    private static final Color CARD_COLOR =
            new Color(45, 52, 62);
    private static final Color TEXT_COLOR =
            new Color(235, 239, 244);
    private static final Color HEX_ACCENT_COLOR =
            new Color(255, 214, 64);
    private static final Color UNIT_ACCENT_COLOR =
            new Color(91, 192, 235);

    private final JLabel coordinateLabel;
    private final JLabel previewLabel;
    private final JLabel terrainLabel;
    private final JLabel resourcesLabel;
    private final JLabel territoryLabel;
    private final JLabel movementLabel;
    private final JLabel passableLabel;
    private final JLabel constructionLabel;
    private final JLabel structureLabel;

    private final JLabel unitTypeLabel;
    private final JLabel unitIdLabel;
    private final JLabel actionPointsLabel;
    private final JLabel visionLabel;
    private final JLabel chargesLabel;
    private final GameImageRepository imageRepository;

    public HexInfoPanel() {
        this(new GameImageRepository());
    }

    public HexInfoPanel(GameImageRepository imageRepository) {
        this.imageRepository = Objects.requireNonNull(
                imageRepository,
                "Image repository cannot be null"
        );
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(BACKGROUND_COLOR);
        setPreferredSize(new Dimension(300, 520));

        setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(
                                0,
                                1,
                                0,
                                0,
                                new Color(55, 64, 75)
                        ),
                        new EmptyBorder(10, 12, 10, 12)
                )
        );

        previewLabel = new JLabel("No image", SwingConstants.CENTER);
        previewLabel.setAlignmentX(0.5f);
        previewLabel.setForeground(TEXT_COLOR);
        previewLabel.setOpaque(true);
        previewLabel.setBackground(CARD_COLOR);
        previewLabel.setBorder(
                BorderFactory.createLineBorder(new Color(62, 71, 83))
        );
        previewLabel.setMinimumSize(new Dimension(96, 82));
        previewLabel.setPreferredSize(new Dimension(96, 82));
        previewLabel.setMaximumSize(new Dimension(120, 82));

        coordinateLabel = createInformationLabel();
        terrainLabel = createInformationLabel();
        resourcesLabel = createInformationLabel();
        territoryLabel = createInformationLabel();
        movementLabel = createInformationLabel();
        passableLabel = createInformationLabel();
        constructionLabel = createInformationLabel();
        structureLabel = createInformationLabel();

        unitTypeLabel = createInformationLabel();
        unitIdLabel = createInformationLabel();
        actionPointsLabel = createInformationLabel();
        visionLabel = createInformationLabel();
        chargesLabel = createInformationLabel();

        add(createSectionTitle(
                "Selected Hex",
                HEX_ACCENT_COLOR
        ));
        add(Box.createVerticalStrut(7));
        add(previewLabel);
        add(Box.createVerticalStrut(8));
        addWithSpacing(coordinateLabel);
        addWithSpacing(terrainLabel);
        addWithSpacing(resourcesLabel);
        addWithSpacing(territoryLabel);
        addWithSpacing(movementLabel);
        addWithSpacing(passableLabel);
        addWithSpacing(constructionLabel);
        addWithSpacing(structureLabel);

        add(Box.createVerticalStrut(8));
        add(createSectionTitle(
                "Selected Unit",
                UNIT_ACCENT_COLOR
        ));
        add(Box.createVerticalStrut(7));
        addWithSpacing(unitTypeLabel);
        addWithSpacing(unitIdLabel);
        addWithSpacing(actionPointsLabel);
        addWithSpacing(visionLabel);
        addWithSpacing(chargesLabel);
        add(Box.createVerticalGlue());

        clearInformation();
    }

    public void showSelection(
            HexTile tile,
            Unit unit,
            ResourceBuilding resourceBuilding,
            Settlement settlement,
            boolean containsTownHall
    ) {
        showSelection(
                tile,
                unit,
                resourceBuilding,
                settlement,
                containsTownHall,
                TownHallLevel.BASE_CAMP
        );
    }

    public void showSelection(
            HexTile tile,
            Unit unit,
            ResourceBuilding resourceBuilding,
            Settlement settlement,
            boolean containsTownHall,
            TownHallLevel townHallLevel
    ) {
        Objects.requireNonNull(
                tile,
                "Hex tile cannot be null"
        );

        refreshPreview(
                tile,
                unit,
                resourceBuilding,
                settlement,
                containsTownHall,
                townHallLevel
        );

        coordinateLabel.setText(
                "Coordinate: (" +
                        tile.getCoordinate().getQ() +
                        ", " +
                        tile.getCoordinate().getR() +
                        ")"
        );

        if (!tile.isDiscovered()) {
            showUnknownHex();
        } else {
            terrainLabel.setText(
                    "Terrain: " +
                            getTerrainDisplayName(
                                    tile.getTerrainType()
                            )
            );

            resourcesLabel.setText(
                    "Resources: " +
                            getResourcesDisplayText(tile)
            );

            territoryLabel.setText(
                    "Territory: " +
                            (tile.isInsidePlayerTerritory()
                                    ? "Player"
                                    : "Outside")
            );

            movementLabel.setText(
                    tile.isNormallyPassable()
                            ? "Movement: " +
                                    tile.getBaseMovementCost() +
                                    " AP"
                            : "Movement: Blocked"
            );

            passableLabel.setText(
                    "Passable: " +
                            getYesNoText(
                                    tile.isNormallyPassable()
                            )
            );

            constructionLabel.setText(
                    "Construction: " +
                            getYesNoText(
                                    tile.isConstructionAllowed()
                            )
            );

            structureLabel.setText(
                    "Structure: " +
                            (containsTownHall
                                    ? "Town Hall"
                                    : getStructureDisplayText(
                                            resourceBuilding,
                                            settlement
                                    ))
            );
        }

        showUnit(unit);
    }

    public void clearInformation() {
        previewLabel.setIcon(null);
        previewLabel.setText("No image");
        coordinateLabel.setText("Coordinate: -");
        terrainLabel.setText("Terrain: -");
        resourcesLabel.setText("Resources: -");
        territoryLabel.setText("Territory: -");
        movementLabel.setText("Movement: -");
        passableLabel.setText("Passable: -");
        constructionLabel.setText("Construction: -");
        structureLabel.setText("Structure: -");
        showUnit(null);
    }

    private void refreshPreview(
            HexTile tile,
            Unit unit,
            ResourceBuilding resourceBuilding,
            Settlement settlement,
            boolean containsTownHall,
            TownHallLevel townHallLevel
    ) {
        BufferedImage image = null;

        if (tile.isDiscovered()) {
            if (unit != null) {
                image = imageRepository.getUnitImage(unit.getUnitType());
            } else if (containsTownHall) {
                image = imageRepository.getTownHallImage(townHallLevel);
            } else if (resourceBuilding != null) {
                image = imageRepository.getBuildingImage(
                        resourceBuilding.getBuildingType()
                );
            } else if (settlement != null) {
                image = imageRepository.getPopulationSettlementImage();
            } else if (!tile.getNaturalResources().isEmpty()) {
                image = imageRepository.getNaturalResourceImage(
                        tile.getNaturalResources().iterator().next()
                );
            } else {
                image = imageRepository.getTerrainImage(
                        tile.getTerrainType()
                );
            }
        }

        BufferedImage scaled = ImageScaler.fit(image, 88, 72);
        previewLabel.setIcon(
                scaled == null ? null : new ImageIcon(scaled)
        );
        previewLabel.setText(scaled == null ? "No image" : "");
    }

    private void showUnknownHex() {
        terrainLabel.setText("Terrain: Unknown");
        resourcesLabel.setText("Resources: Unknown");
        territoryLabel.setText("Territory: Unknown");
        movementLabel.setText("Movement: Unknown");
        passableLabel.setText("Passable: Unknown");
        constructionLabel.setText("Construction: Unknown");
        structureLabel.setText("Structure: Unknown");
    }

    private void showUnit(Unit unit) {
        if (unit == null) {
            unitTypeLabel.setText("Type: None");
            unitIdLabel.setText("Unit ID: -");
            actionPointsLabel.setText("AP: -");
            visionLabel.setText("Vision: -");
            chargesLabel.setText("Builder charges: -");
            return;
        }

        unitTypeLabel.setText(
                "Type: " + unit.getDisplayName()
        );
        unitIdLabel.setText(
                "Unit ID: " + unit.getId()
        );
        actionPointsLabel.setText(
                "AP: " +
                        unit.getCurrentActionPoints() +
                        "/" +
                        unit.getMaximumActionPoints()
        );
        visionLabel.setText(
                "Vision radius: " +
                        unit.getVisionRadius()
        );

        if (unit instanceof Builder) {
            Builder builder = (Builder) unit;
            chargesLabel.setText(
                    "Builder charges: " +
                            builder.getRemainingCharges()
            );
        } else if (unit instanceof Worker) {
            Worker worker = (Worker) unit;

            chargesLabel.setText(
                    worker.isStationed()
                            ? "Stationed: Building #" +
                                    worker.getStationedBuildingId()
                            : "Stationed: No"
            );
        } else {
            chargesLabel.setText(
                    "Builder charges: N/A"
            );
        }
    }

    private String getStructureDisplayText(
            ResourceBuilding resourceBuilding,
            Settlement settlement
    ) {
        if (settlement != null) {
            return "Settlement (Unit Cap +" +
                    settlement.getUnitCapBonus() +
                    ", upkeep " +
                    settlement
                            .getConsecutiveMissedUpkeepTurns() +
                    "/" +
                    Settlement
                            .MAXIMUM_CONSECUTIVE_MISSED_UPKEEP_TURNS +
                    ")";
        }

        if (resourceBuilding == null) {
            return "None";
        }

        return resourceBuilding.getBuildingType()
                .getDisplayName() +
                " (workers " +
                resourceBuilding.getStationedWorkerCount() +
                "/" +
                resourceBuilding.getWorkerCapacity() +
                ", upkeep " +
                resourceBuilding
                        .getConsecutiveMissedUpkeepTurns() +
                "/" +
                ResourceBuilding
                        .MAXIMUM_CONSECUTIVE_MISSED_UPKEEP_TURNS +
                ")";
    }

    private JLabel createSectionTitle(
            String text,
            Color color
    ) {
        JLabel label =
                new JLabel(text, SwingConstants.CENTER);
        label.setFont(
                new Font(
                        Font.SANS_SERIF,
                        Font.BOLD,
                        17
                )
        );
        label.setForeground(color);
        label.setAlignmentX(0.5f);
        return label;
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
                        12
                )
        );
        label.setHorizontalAlignment(SwingConstants.LEFT);
        label.setAlignmentX(0.5f);
        label.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                new Color(62, 71, 83)
                        ),
                        new EmptyBorder(5, 8, 5, 8)
                )
        );
        label.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 30)
        );
        return label;
    }

    private void addWithSpacing(JLabel label) {
        add(label);
        add(Box.createVerticalStrut(4));
    }

    private String getTerrainDisplayName(
            TerrainType terrain
    ) {
        switch (terrain) {
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

    private String getResourcesDisplayText(HexTile tile) {
        if (tile.getNaturalResources().isEmpty()) {
            return "None";
        }

        StringBuilder builder = new StringBuilder();

        for (NaturalResourceType resourceType :
                tile.getNaturalResources()) {

            if (builder.length() > 0) {
                builder.append(", ");
            }

            builder.append(
                    getResourceDisplayName(resourceType)
            );
            builder.append(' ');

            if (tile.isNaturalResourceDepleted(resourceType)) {
                builder.append("DEPLETED (0/");
                builder.append(
                        tile.getInitialResourceAmount(
                                resourceType
                        )
                );
                builder.append(')');
            } else {
                builder.append(
                        tile.getRemainingResourceAmount(
                                resourceType
                        )
                );
                builder.append('/');
                builder.append(
                        tile.getInitialResourceAmount(
                                resourceType
                        )
                );
            }
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
