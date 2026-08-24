package ir.ac.ap.civilization.view;

import ir.ac.ap.civilization.controller.GameController;
import ir.ac.ap.civilization.model.game.GameState;
import ir.ac.ap.civilization.model.map.GameMap;
import ir.ac.ap.civilization.model.map.HexCoordinate;
import ir.ac.ap.civilization.model.map.HexTile;
import ir.ac.ap.civilization.model.map.NaturalResourceType;
import ir.ac.ap.civilization.model.map.TerrainType;
import ir.ac.ap.civilization.model.unit.Unit;
import ir.ac.ap.civilization.model.unit.UnitMovementResult;
import ir.ac.ap.civilization.model.unit.UnitType;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;

public final class HexMapPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private static final Color BACKGROUND_COLOR =
            new Color(22, 27, 34);
    private static final Color UNKNOWN_COLOR =
            new Color(12, 16, 21);
    private static final Color GRID_COLOR =
            new Color(25, 31, 38);
    private static final Color SELECTION_COLOR =
            new Color(255, 214, 64);
    private static final Color DESTINATION_COLOR =
            new Color(87, 225, 126);
    private static final Color TOWN_HALL_COLOR =
            new Color(176, 65, 62);
    private static final Color EXPLORED_OVERLAY =
            new Color(5, 9, 14, 165);
    private static final Color TERRITORY_TINT =
            new Color(255, 196, 76, 28);
    private static final Color TERRITORY_BORDER_COLOR =
            new Color(255, 196, 76, 225);
    private static final Color DEPLETED_RESOURCE_COLOR =
            new Color(75, 78, 83);

    private static final int[][] TERRITORY_EDGE_VERTICES = {
            {1, 2},
            {0, 1},
            {5, 0},
            {4, 5},
            {3, 4},
            {2, 3}
    };

    private static final double SQRT_THREE =
            Math.sqrt(3.0);

    private static final double BASE_HEX_SIZE = 34.0;

    private static final double[] ZOOM_LEVELS = {
            0.65,
            0.80,
            1.00,
            1.25,
            1.50
    };

    private static final int ANIMATION_DELAY_MS = 16;
    private static final int ANIMATION_DURATION_MS = 320;

    private final GameController controller;
    private final GameState gameState;
    private final GameMap gameMap;
    private final HexCoordinate townHallCoordinate;

    private final Set<HexCoordinate> validDestinations;

    private HexTile selectedTile;
    private Unit selectedUnit;

    private BiConsumer<HexTile, Unit> selectionListener;
    private BiConsumer<String, Boolean> statusListener;

    private int zoomIndex;
    private double panX;
    private double panY;
    private boolean panning;
    private Point lastDragPoint;

    private Timer movementTimer;
    private int movingUnitId;
    private HexCoordinate animationStart;
    private HexCoordinate animationDestination;
    private double animationProgress;

    public HexMapPanel(GameController controller) {
        this.controller = Objects.requireNonNull(
                controller,
                "Game controller cannot be null"
        );

        this.gameState = controller.getGameState();
        this.gameMap = gameState.getGameMap();

        this.townHallCoordinate =
                gameState.getTownHallCoordinate();

        this.validDestinations = new HashSet<>();

        this.selectionListener = (tile, unit) -> {
        };

        this.statusListener = (message, error) -> {
        };

        this.zoomIndex = 0;
        this.movingUnitId = -1;

        this.selectedTile = gameMap
                .getTile(townHallCoordinate)
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Town Hall must be inside the map"
                        )
                );

        configurePanel();
        installMouseControls();
    }

    public void setSelectionListener(
            BiConsumer<HexTile, Unit> selectionListener
    ) {
        this.selectionListener = Objects.requireNonNull(
                selectionListener,
                "Selection listener cannot be null"
        );

        notifySelectionChanged();
    }

    public void setStatusListener(
            BiConsumer<String, Boolean> statusListener
    ) {
        this.statusListener = Objects.requireNonNull(
                statusListener,
                "Status listener cannot be null"
        );
    }

    public HexTile getSelectedTile() {
        return selectedTile;
    }

    public Unit getSelectedUnit() {
        return selectedUnit;
    }

    public double getZoomLevel() {
        return ZOOM_LEVELS[zoomIndex];
    }

    public void resetCamera() {
        zoomIndex = 0;
        panX = 0.0;
        panY = 0.0;
        repaint();
    }

    public void refreshFromModel() {
        finishAnimationImmediately();
        refreshValidDestinations();
        notifySelectionChanged();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);

        Graphics2D graphics2D =
                (Graphics2D) graphics.create();

        try {
            configureGraphics(graphics2D);

            for (HexTile tile : gameMap.getTiles()) {
                drawTile(graphics2D, tile);
            }

            drawUnits(graphics2D);
            drawMovingUnit(graphics2D);
        } finally {
            graphics2D.dispose();
        }
    }

    private void configurePanel() {
        setBackground(BACKGROUND_COLOR);
        setPreferredSize(new Dimension(800, 500));

        setBorder(
                BorderFactory.createLineBorder(
                        new Color(55, 64, 75)
                )
        );

        setToolTipText(
                "Left click: select or move | " +
                        "Right/middle drag: pan | " +
                        "Wheel: zoom"
        );

        setCursor(
                Cursor.getPredefinedCursor(
                        Cursor.HAND_CURSOR
                )
        );
    }

    private void configureGraphics(
            Graphics2D graphics2D
    ) {
        graphics2D.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );

        graphics2D.setRenderingHint(
                RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON
        );
    }

    private void drawTile(
            Graphics2D graphics2D,
            HexTile tile
    ) {
        Point center =
                getTileCenter(tile.getCoordinate());

        Polygon polygon = createHexPolygon(center);

        if (!tile.isDiscovered()) {
            graphics2D.setColor(UNKNOWN_COLOR);
            graphics2D.fillPolygon(polygon);
            drawGrid(graphics2D, polygon);
            return;
        }

        graphics2D.setColor(
                getTerrainColor(
                        tile.getTerrainType()
                )
        );

        graphics2D.fillPolygon(polygon);
        drawGrid(graphics2D, polygon);

        drawTerrainDetail(
                graphics2D,
                tile.getTerrainType(),
                center
        );

        drawTerritoryTint(
                graphics2D,
                tile,
                polygon
        );

        if (tile.getCoordinate().equals(
                townHallCoordinate
        )) {
            drawTownHall(graphics2D, center);
        } else {
            drawNaturalResources(
                    graphics2D,
                    tile,
                    center
            );
        }

        if (!tile.isVisible()) {
            graphics2D.setColor(EXPLORED_OVERLAY);
            graphics2D.fillPolygon(polygon);
        }

        drawTerritoryBoundary(
                graphics2D,
                tile,
                polygon
        );

        if (validDestinations.contains(
                tile.getCoordinate()
        )) {
            graphics2D.setColor(
                    new Color(87, 225, 126, 75)
            );

            graphics2D.fillPolygon(polygon);
            graphics2D.setColor(DESTINATION_COLOR);

            graphics2D.setStroke(
                    new BasicStroke(
                            (float) Math.max(
                                    2.0,
                                    3.0 * getZoomLevel()
                            )
                    )
            );

            graphics2D.drawPolygon(polygon);
        }

        if (tile == selectedTile) {
            graphics2D.setColor(SELECTION_COLOR);

            graphics2D.setStroke(
                    new BasicStroke(
                            (float) Math.max(
                                    3.0,
                                    4.0 * getZoomLevel()
                            )
                    )
            );

            graphics2D.drawPolygon(polygon);
        }
    }

    private void drawGrid(
            Graphics2D graphics2D,
            Polygon polygon
    ) {
        graphics2D.setColor(GRID_COLOR);

        graphics2D.setStroke(
                new BasicStroke(
                        (float) Math.max(
                                1.0,
                                getZoomLevel()
                        )
                )
        );

        graphics2D.drawPolygon(polygon);
    }

    private void drawTerritoryTint(
            Graphics2D graphics2D,
            HexTile tile,
            Polygon polygon
    ) {
        if (!tile.isInsidePlayerTerritory()) {
            return;
        }

        graphics2D.setColor(TERRITORY_TINT);
        graphics2D.fillPolygon(polygon);
    }

    private void drawTerritoryBoundary(
            Graphics2D graphics2D,
            HexTile tile,
            Polygon polygon
    ) {
        if (!tile.isInsidePlayerTerritory()) {
            return;
        }

        graphics2D.setColor(
                TERRITORY_BORDER_COLOR
        );

        graphics2D.setStroke(
                new BasicStroke(
                        (float) Math.max(
                                2.0,
                                2.5 * getZoomLevel()
                        ),
                        BasicStroke.CAP_ROUND,
                        BasicStroke.JOIN_ROUND
                )
        );

        List<HexCoordinate> neighbors =
                tile.getCoordinate().getNeighbors();

        for (int index = 0;
             index < neighbors.size();
             index++) {

            HexTile neighborTile = gameMap
                    .getTile(neighbors.get(index))
                    .orElse(null);

            if (neighborTile != null &&
                    neighborTile
                            .isInsidePlayerTerritory()) {
                continue;
            }

            int startVertex =
                    TERRITORY_EDGE_VERTICES[index][0];

            int endVertex =
                    TERRITORY_EDGE_VERTICES[index][1];

            graphics2D.drawLine(
                    polygon.xpoints[startVertex],
                    polygon.ypoints[startVertex],
                    polygon.xpoints[endVertex],
                    polygon.ypoints[endVertex]
            );
        }
    }

    private void drawUnits(Graphics2D graphics2D) {
        for (Unit unit : gameState.getUnits()) {
            if (unit.getId() == movingUnitId) {
                continue;
            }

            HexTile tile = gameMap
                    .getTile(unit.getPosition())
                    .orElse(null);

            if (tile == null || !tile.isVisible()) {
                continue;
            }

            List<Unit> unitsAtPosition =
                    gameState.getUnitsAt(
                            unit.getPosition()
                    );

            int index =
                    unitsAtPosition.indexOf(unit);

            Point center =
                    getTileCenter(unit.getPosition());

            Point unitCenter = offsetUnitCenter(
                    center,
                    index,
                    unitsAtPosition.size()
            );

            drawUnitMarker(
                    graphics2D,
                    unit,
                    unitCenter,
                    unit == selectedUnit
            );
        }
    }

    private void drawMovingUnit(
            Graphics2D graphics2D
    ) {
        if (movingUnitId < 0 ||
                animationStart == null ||
                animationDestination == null) {
            return;
        }

        Unit unit = gameState
                .getUnitById(movingUnitId)
                .orElse(null);

        if (unit == null) {
            return;
        }

        Point start =
                getTileCenter(animationStart);

        Point destination =
                getTileCenter(animationDestination);

        int x = (int) Math.round(
                start.x +
                        (destination.x - start.x) *
                                animationProgress
        );

        int y = (int) Math.round(
                start.y +
                        (destination.y - start.y) *
                                animationProgress
        );

        drawUnitMarker(
                graphics2D,
                unit,
                new Point(x, y),
                true
        );
    }

    private Point offsetUnitCenter(
            Point center,
            int index,
            int count
    ) {
        if (count <= 1) {
            return center;
        }

        double spacing =
                15.0 * getZoomLevel();

        double firstOffset =
                -((count - 1) * spacing) / 2.0;

        return new Point(
                (int) Math.round(
                        center.x +
                                firstOffset +
                                index * spacing
                ),
                center.y
        );
    }

    private void drawUnitMarker(
            Graphics2D graphics2D,
            Unit unit,
            Point center,
            boolean selected
    ) {
        int diameter = Math.max(
                17,
                (int) Math.round(
                        25.0 * getZoomLevel()
                )
        );

        int x = center.x - diameter / 2;
        int y = center.y - diameter / 2;

        if (selected) {
            graphics2D.setColor(
                    new Color(91, 192, 235)
            );

            graphics2D.fillOval(
                    x - 3,
                    y - 3,
                    diameter + 6,
                    diameter + 6
            );
        }

        graphics2D.setColor(
                getUnitColor(unit.getUnitType())
        );

        graphics2D.fillOval(
                x,
                y,
                diameter,
                diameter
        );

        graphics2D.setColor(Color.WHITE);

        graphics2D.setStroke(
                new BasicStroke(
                        (float) Math.max(
                                1.0,
                                1.5 * getZoomLevel()
                        )
                )
        );

        graphics2D.drawOval(
                x,
                y,
                diameter,
                diameter
        );

        graphics2D.setFont(
                new Font(
                        Font.SANS_SERIF,
                        Font.BOLD,
                        Math.max(
                                9,
                                (int) Math.round(
                                        12 * getZoomLevel()
                                )
                        )
                )
        );

        String symbol = unit.getMapSymbol();

        int textWidth = graphics2D
                .getFontMetrics()
                .stringWidth(symbol);

        int textY = center.y +
                graphics2D.getFontMetrics()
                        .getAscent() / 2 - 2;

        graphics2D.drawString(
                symbol,
                center.x - textWidth / 2,
                textY
        );
    }

    private Color getUnitColor(UnitType unitType) {
        switch (unitType) {
            case BUILDER:
                return new Color(205, 125, 55);
            case WORKER:
                return new Color(70, 145, 205);
            case EXPLORER:
                return new Color(151, 82, 184);
            default:
                throw new IllegalStateException(
                        "Unknown unit type"
                );
        }
    }

    private Point getTileCenter(
            HexCoordinate coordinate
    ) {
        double worldX =
                BASE_HEX_SIZE *
                        SQRT_THREE *
                        (coordinate.getQ() +
                                coordinate.getR() / 2.0);

        double worldY =
                BASE_HEX_SIZE *
                        1.5 *
                        coordinate.getR();

        return new Point(
                (int) Math.round(
                        getWidth() / 2.0 +
                                panX +
                                worldX * getZoomLevel()
                ),
                (int) Math.round(
                        getHeight() / 2.0 +
                                panY +
                                worldY * getZoomLevel()
                )
        );
    }

    private Polygon createHexPolygon(Point center) {
        Polygon polygon = new Polygon();

        double size =
                BASE_HEX_SIZE * getZoomLevel();

        for (int index = 0; index < 6; index++) {
            double angle = Math.toRadians(
                    60.0 * index - 90.0
            );

            polygon.addPoint(
                    (int) Math.round(
                            center.x +
                                    size * Math.cos(angle)
                    ),
                    (int) Math.round(
                            center.y +
                                    size * Math.sin(angle)
                    )
            );
        }

        return polygon;
    }

    private Color getTerrainColor(
            TerrainType terrain
    ) {
        switch (terrain) {
            case FOREST:
                return new Color(55, 116, 72);
            case PLAIN:
                return new Color(190, 166, 101);
            case MOUNTAIN:
                return new Color(126, 118, 110);
            case GRASSLAND:
                return new Color(105, 151, 76);
            case SEA:
                return new Color(57, 113, 159);
            case MOUNTAIN_RANGE:
                return new Color(68, 72, 78);
            default:
                throw new IllegalStateException(
                        "Unknown terrain type"
                );
        }
    }

    private void drawTerrainDetail(
            Graphics2D graphics2D,
            TerrainType terrain,
            Point center
    ) {
        int size = Math.max(
                5,
                (int) Math.round(
                        8.0 * getZoomLevel()
                )
        );

        graphics2D.setStroke(
                new BasicStroke(
                        (float) Math.max(
                                1.0,
                                1.5 * getZoomLevel()
                        )
                )
        );

        switch (terrain) {
            case FOREST:
                graphics2D.setColor(
                        new Color(31, 77, 44)
                );

                graphics2D.fillOval(
                        center.x - size - 3,
                        center.y - size,
                        size,
                        size
                );

                graphics2D.fillOval(
                        center.x + 3,
                        center.y - size,
                        size,
                        size
                );
                break;

            case PLAIN:
                graphics2D.setColor(
                        new Color(145, 121, 64)
                );

                graphics2D.drawLine(
                        center.x - size,
                        center.y,
                        center.x + size,
                        center.y
                );
                break;

            case MOUNTAIN:
                graphics2D.setColor(
                        new Color(76, 72, 69)
                );

                drawMountainPeak(
                        graphics2D,
                        center.x,
                        center.y,
                        size
                );
                break;

            case GRASSLAND:
                graphics2D.setColor(
                        new Color(67, 112, 46)
                );

                graphics2D.drawLine(
                        center.x - 4,
                        center.y + 3,
                        center.x,
                        center.y - size
                );

                graphics2D.drawLine(
                        center.x + 4,
                        center.y + 3,
                        center.x,
                        center.y - size
                );
                break;

            case SEA:
                graphics2D.setColor(
                        new Color(172, 214, 235)
                );

                graphics2D.drawArc(
                        center.x - size,
                        center.y - 3,
                        size,
                        Math.max(3, size / 2),
                        0,
                        180
                );

                graphics2D.drawArc(
                        center.x,
                        center.y - 3,
                        size,
                        Math.max(3, size / 2),
                        0,
                        180
                );
                break;

            case MOUNTAIN_RANGE:
                graphics2D.setColor(
                        new Color(205, 210, 216)
                );

                drawMountainPeak(
                        graphics2D,
                        center.x - size / 2,
                        center.y,
                        size
                );

                drawMountainPeak(
                        graphics2D,
                        center.x + size / 2,
                        center.y,
                        size
                );
                break;

            default:
                throw new IllegalStateException(
                        "Unknown terrain type"
                );
        }
    }

    private void drawMountainPeak(
            Graphics2D graphics2D,
            int centerX,
            int centerY,
            int size
    ) {
        Polygon peak = new Polygon();

        peak.addPoint(
                centerX,
                centerY - size
        );

        peak.addPoint(
                centerX - size,
                centerY + size / 2
        );

        peak.addPoint(
                centerX + size,
                centerY + size / 2
        );

        graphics2D.drawPolygon(peak);
    }

    private void drawTownHall(
            Graphics2D graphics2D,
            Point center
    ) {
        double zoom = getZoomLevel();

        int width =
                (int) Math.round(34.0 * zoom);

        int height =
                (int) Math.round(25.0 * zoom);

        int x = center.x - width / 2;
        int y = center.y - height / 2;

        graphics2D.setColor(TOWN_HALL_COLOR);

        graphics2D.fillRoundRect(
                x,
                y,
                width,
                height,
                6,
                6
        );

        graphics2D.setColor(
                new Color(249, 231, 194)
        );

        graphics2D.drawRoundRect(
                x,
                y,
                width,
                height,
                6,
                6
        );

        drawCenteredText(
                graphics2D,
                "TH",
                center,
                10
        );
    }

    private void drawNaturalResources(
            Graphics2D graphics2D,
            HexTile tile,
            Point center
    ) {
        Set<NaturalResourceType> resources =
                tile.getNaturalResources();

        if (resources.isEmpty()) {
            return;
        }

        int size = Math.max(
                11,
                (int) Math.round(
                        16.0 * getZoomLevel()
                )
        );

        int gap = 2;

        int totalWidth =
                resources.size() * size +
                        (resources.size() - 1) * gap;

        int x = center.x - totalWidth / 2;

        int y = center.y +
                (int) Math.round(
                        10.0 * getZoomLevel()
                );

        for (NaturalResourceType resource : resources) {
            boolean depleted =
                    tile.isNaturalResourceDepleted(
                            resource
                    );

            graphics2D.setColor(
                    depleted
                            ? DEPLETED_RESOURCE_COLOR
                            : getResourceColor(resource)
            );

            graphics2D.fillOval(
                    x,
                    y,
                    size,
                    size
            );

            graphics2D.setColor(Color.WHITE);

            graphics2D.setFont(
                    new Font(
                            Font.SANS_SERIF,
                            Font.BOLD,
                            8
                    )
            );

            String text =
                    getResourceAbbreviation(resource);

            int textWidth = graphics2D
                    .getFontMetrics()
                    .stringWidth(text);

            graphics2D.drawString(
                    text,
                    x + (size - textWidth) / 2,
                    y + size - 3
            );

            if (depleted) {
                graphics2D.setColor(
                        new Color(230, 95, 95)
                );

                graphics2D.setStroke(
                        new BasicStroke(
                                (float) Math.max(
                                        1.0,
                                        getZoomLevel()
                                )
                        )
                );

                graphics2D.drawLine(
                        x + 2,
                        y + 2,
                        x + size - 2,
                        y + size - 2
                );
            }

            x += size + gap;
        }
    }

    private Color getResourceColor(
            NaturalResourceType resource
    ) {
        switch (resource) {
            case WOOD:
                return new Color(102, 67, 42);
            case STONE:
                return new Color(105, 110, 117);
            case IRON:
                return new Color(65, 73, 82);
            case WHEAT:
            case RICE:
                return new Color(206, 164, 57);
            case CATTLE:
            case SHEEP:
                return new Color(139, 91, 62);
            case FISH:
                return new Color(31, 92, 139);
            default:
                throw new IllegalStateException(
                        "Unknown resource"
                );
        }
    }

    private String getResourceAbbreviation(
            NaturalResourceType resource
    ) {
        switch (resource) {
            case WOOD:
                return "W";
            case STONE:
                return "S";
            case IRON:
                return "I";
            case WHEAT:
                return "Wh";
            case RICE:
                return "R";
            case CATTLE:
                return "C";
            case SHEEP:
                return "Sh";
            case FISH:
                return "F";
            default:
                throw new IllegalStateException(
                        "Unknown resource"
                );
        }
    }

    private void drawCenteredText(
            Graphics2D graphics2D,
            String text,
            Point center,
            int baseFontSize
    ) {
        graphics2D.setFont(
                new Font(
                        Font.SANS_SERIF,
                        Font.BOLD,
                        Math.max(
                                8,
                                (int) Math.round(
                                        baseFontSize *
                                                getZoomLevel()
                                )
                        )
                )
        );

        int width = graphics2D
                .getFontMetrics()
                .stringWidth(text);

        int y = center.y +
                graphics2D.getFontMetrics()
                        .getAscent() / 2 - 2;

        graphics2D.drawString(
                text,
                center.x - width / 2,
                y
        );
    }

    private void installMouseControls() {
        MouseAdapter adapter = new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent event) {
                if (SwingUtilities
                        .isRightMouseButton(event) ||
                        SwingUtilities
                                .isMiddleMouseButton(event)) {

                    panning = true;
                    lastDragPoint = event.getPoint();

                    setCursor(
                            Cursor.getPredefinedCursor(
                                    Cursor.MOVE_CURSOR
                            )
                    );
                }
            }

            @Override
            public void mouseReleased(MouseEvent event) {
                if (panning) {
                    panning = false;
                    lastDragPoint = null;

                    setCursor(
                            Cursor.getPredefinedCursor(
                                    Cursor.HAND_CURSOR
                            )
                    );
                }
            }

            @Override
            public void mouseDragged(MouseEvent event) {
                if (!panning ||
                        lastDragPoint == null) {
                    return;
                }

                panX +=
                        event.getX() - lastDragPoint.x;

                panY +=
                        event.getY() - lastDragPoint.y;

                lastDragPoint = event.getPoint();
                repaint();
            }

            @Override
            public void mouseClicked(MouseEvent event) {
                if (SwingUtilities
                        .isLeftMouseButton(event)) {

                    handleLeftClick(
                            event.getPoint()
                    );
                }
            }

            @Override
            public void mouseWheelMoved(
                    MouseWheelEvent event
            ) {
                changeZoom(event);
            }
        };

        addMouseListener(adapter);
        addMouseMotionListener(adapter);
        addMouseWheelListener(adapter);
    }

    private void handleLeftClick(Point point) {
        if (isAnimationRunning()) {
            statusListener.accept(
                    "Wait for the movement animation to finish",
                    true
            );
            return;
        }

        HexTile clickedTile = findTileAt(point);

        if (clickedTile == null ||
                !clickedTile.isDiscovered()) {
            return;
        }

        List<Unit> visibleUnits =
                clickedTile.isVisible()
                        ? gameState.getUnitsAt(
                        clickedTile.getCoordinate()
                )
                        : Collections
                        .<Unit>emptyList();

        if (!visibleUnits.isEmpty()) {
            selectedTile = clickedTile;

            selectedUnit =
                    chooseUnitFromTile(visibleUnits);

            refreshValidDestinations();
            notifySelectionChanged();

            statusListener.accept(
                    selectedUnit.getDisplayName() +
                            " selected",
                    false
            );

            repaint();
            return;
        }

        if (selectedUnit != null &&
                validDestinations.contains(
                        clickedTile.getCoordinate()
                )) {

            performMovement(clickedTile);
            return;
        }

        selectedTile = clickedTile;
        selectedUnit = null;
        validDestinations.clear();

        notifySelectionChanged();
        repaint();
    }

    private Unit chooseUnitFromTile(
            List<Unit> units
    ) {
        if (selectedUnit == null ||
                !units.contains(selectedUnit)) {

            return units.get(0);
        }

        int currentIndex =
                units.indexOf(selectedUnit);

        return units.get(
                (currentIndex + 1) %
                        units.size()
        );
    }

    private HexTile findTileAt(Point point) {
        for (HexTile tile : gameMap.getTiles()) {
            Polygon polygon = createHexPolygon(
                    getTileCenter(
                            tile.getCoordinate()
                    )
            );

            if (polygon.contains(point)) {
                return tile;
            }
        }

        return null;
    }

    private void performMovement(
            HexTile destinationTile
    ) {
        try {
            UnitMovementResult result =
                    controller.moveUnit(
                            selectedUnit.getId(),
                            destinationTile
                                    .getCoordinate()
                    );

            selectedTile = destinationTile;
            validDestinations.clear();

            notifySelectionChanged();

            statusListener.accept(
                    selectedUnit.getDisplayName() +
                            " moved for " +
                            result.getActionPointCost() +
                            " AP",
                    false
            );

            startMovementAnimation(result);
        } catch (
                IllegalArgumentException |
                IllegalStateException exception
        ) {
            statusListener.accept(
                    exception.getMessage(),
                    true
            );

            refreshValidDestinations();
            repaint();
        }
    }

    private void refreshValidDestinations() {
        validDestinations.clear();

        if (selectedUnit == null) {
            return;
        }

        for (HexTile tile :
                controller.getValidMovementDestinations(
                        selectedUnit.getId()
                )) {

            validDestinations.add(
                    tile.getCoordinate()
            );
        }
    }

    private void startMovementAnimation(
            UnitMovementResult result
    ) {
        finishAnimationImmediately();

        movingUnitId = result.getUnitId();
        animationStart = result.getStart();

        animationDestination =
                result.getDestination();

        animationProgress = 0.0;

        movementTimer = new Timer(
                ANIMATION_DELAY_MS,
                event -> advanceAnimation()
        );

        movementTimer.start();
        repaint();
    }

    private void advanceAnimation() {
        animationProgress +=
                (double) ANIMATION_DELAY_MS /
                        ANIMATION_DURATION_MS;

        if (animationProgress >= 1.0) {
            animationProgress = 1.0;
            movementTimer.stop();
            movingUnitId = -1;
            animationStart = null;
            animationDestination = null;

            refreshValidDestinations();
            notifySelectionChanged();
        }

        repaint();
    }

    private boolean isAnimationRunning() {
        return movementTimer != null &&
                movementTimer.isRunning();
    }

    private void finishAnimationImmediately() {
        if (movementTimer != null &&
                movementTimer.isRunning()) {

            movementTimer.stop();
        }

        movingUnitId = -1;
        animationStart = null;
        animationDestination = null;
        animationProgress = 0.0;
    }

    private void notifySelectionChanged() {
        selectionListener.accept(
                selectedTile,
                selectedUnit
        );
    }

    private void changeZoom(
            MouseWheelEvent event
    ) {
        int direction = Integer.compare(
                event.getWheelRotation(),
                0
        );

        int newZoomIndex = Math.max(
                0,
                Math.min(
                        ZOOM_LEVELS.length - 1,
                        zoomIndex - direction
                )
        );

        if (direction == 0 ||
                newZoomIndex == zoomIndex) {
            return;
        }

        double oldZoom = getZoomLevel();

        double worldX =
                (event.getX() -
                        getWidth() / 2.0 -
                        panX) / oldZoom;

        double worldY =
                (event.getY() -
                        getHeight() / 2.0 -
                        panY) / oldZoom;

        zoomIndex = newZoomIndex;

        panX = event.getX() -
                getWidth() / 2.0 -
                worldX * getZoomLevel();

        panY = event.getY() -
                getHeight() / 2.0 -
                worldY * getZoomLevel();

        repaint();
    }
}