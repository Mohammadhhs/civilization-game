package ir.ac.ap.civilization.view;

import ir.ac.ap.civilization.model.map.GameMap;
import ir.ac.ap.civilization.model.map.HexCoordinate;
import ir.ac.ap.civilization.model.map.HexTile;
import ir.ac.ap.civilization.model.map.NaturalResourceType;
import ir.ac.ap.civilization.model.map.TerrainType;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
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
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

public final class HexMapPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private static final Color BACKGROUND_COLOR =
            new Color(22, 27, 34);

    private static final Color GRID_COLOR =
            new Color(25, 31, 38);

    private static final Color SELECTION_COLOR =
            new Color(255, 214, 64);

    private static final Color TOWN_HALL_COLOR =
            new Color(176, 65, 62);

    private static final double SQRT_THREE = Math.sqrt(3.0);
    private static final double BASE_HEX_SIZE = 34.0;

    private static final double[] ZOOM_LEVELS = {
            0.65,
            0.80,
            1.00,
            1.25,
            1.50
    };

    private final GameMap gameMap;
    private final HexCoordinate townHallCoordinate;

    private HexTile selectedTile;
    private Consumer<HexTile> selectionListener;

    private int zoomIndex;
    private double panX;
    private double panY;

    private boolean panning;
    private Point lastDragPoint;

    public HexMapPanel(
            GameMap gameMap,
            HexCoordinate townHallCoordinate
    ) {
        this.gameMap = Objects.requireNonNull(
                gameMap,
                "Game map cannot be null"
        );

        this.townHallCoordinate = Objects.requireNonNull(
                townHallCoordinate,
                "Town Hall coordinate cannot be null"
        );

        this.zoomIndex = 0;

        this.selectionListener = tile -> {
        };

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
            Consumer<HexTile> selectionListener
    ) {
        this.selectionListener = Objects.requireNonNull(
                selectionListener,
                "Selection listener cannot be null"
        );

        selectionListener.accept(selectedTile);
    }

    public HexTile getSelectedTile() {
        return selectedTile;
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
        } finally {
            graphics2D.dispose();
        }
    }

    private void configurePanel() {
        setBackground(BACKGROUND_COLOR);
        setPreferredSize(new Dimension(820, 500));

        setBorder(
                BorderFactory.createLineBorder(
                        new Color(55, 64, 75)
                )
        );

        setToolTipText(
                "Left click: select | " +
                        "Right or middle drag: pan | " +
                        "Mouse wheel: zoom"
        );

        setCursor(
                Cursor.getPredefinedCursor(
                        Cursor.HAND_CURSOR
                )
        );
    }

    private void configureGraphics(Graphics2D graphics2D) {
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
        Point center = getTileCenter(tile.getCoordinate());
        Polygon polygon = createHexPolygon(center);

        graphics2D.setColor(
                getTerrainColor(tile.getTerrainType())
        );

        graphics2D.fillPolygon(polygon);

        graphics2D.setStroke(
                new BasicStroke(
                        (float) Math.max(
                                1.0,
                                getZoomLevel()
                        )
                )
        );

        graphics2D.setColor(GRID_COLOR);
        graphics2D.drawPolygon(polygon);

        drawTerrainDetail(
                graphics2D,
                tile.getTerrainType(),
                center
        );

        if (tile.getCoordinate().equals(townHallCoordinate)) {
            drawTownHall(graphics2D, center);
        } else {
            drawNaturalResources(
                    graphics2D,
                    tile.getNaturalResources(),
                    center
            );
        }

        if (tile == selectedTile) {
            graphics2D.setStroke(
                    new BasicStroke(
                            (float) Math.max(
                                    3.0,
                                    4.0 * getZoomLevel()
                            )
                    )
            );

            graphics2D.setColor(SELECTION_COLOR);
            graphics2D.drawPolygon(polygon);
        }
    }

    private Point getTileCenter(HexCoordinate coordinate) {
        double worldX =
                BASE_HEX_SIZE *
                        SQRT_THREE *
                        (
                                coordinate.getQ() +
                                        coordinate.getR() / 2.0
                        );

        double worldY =
                BASE_HEX_SIZE *
                        1.5 *
                        coordinate.getR();

        int screenX = (int) Math.round(
                getWidth() / 2.0 +
                        panX +
                        worldX * getZoomLevel()
        );

        int screenY = (int) Math.round(
                getHeight() / 2.0 +
                        panY +
                        worldY * getZoomLevel()
        );

        return new Point(screenX, screenY);
    }

    private Polygon createHexPolygon(Point center) {
        Polygon polygon = new Polygon();

        double size =
                BASE_HEX_SIZE * getZoomLevel();

        for (int index = 0; index < 6; index++) {
            double angle = Math.toRadians(
                    60.0 * index - 90.0
            );

            int x = (int) Math.round(
                    center.x +
                            size * Math.cos(angle)
            );

            int y = (int) Math.round(
                    center.y +
                            size * Math.sin(angle)
            );

            polygon.addPoint(x, y);
        }

        return polygon;
    }

    private Color getTerrainColor(
            TerrainType terrainType
    ) {
        switch (terrainType) {
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
            TerrainType terrainType,
            Point center
    ) {
        double zoom = getZoomLevel();

        int detailSize =
                (int) Math.round(8.0 * zoom);

        graphics2D.setStroke(
                new BasicStroke(
                        (float) Math.max(
                                1.0,
                                1.5 * zoom
                        )
                )
        );

        switch (terrainType) {
            case FOREST:
                graphics2D.setColor(
                        new Color(31, 77, 44)
                );

                graphics2D.fillOval(
                        center.x - detailSize - 4,
                        center.y - detailSize,
                        detailSize,
                        detailSize
                );

                graphics2D.fillOval(
                        center.x + 4,
                        center.y - detailSize,
                        detailSize,
                        detailSize
                );
                break;

            case PLAIN:
                graphics2D.setColor(
                        new Color(145, 121, 64)
                );

                graphics2D.drawLine(
                        center.x - detailSize,
                        center.y,
                        center.x + detailSize,
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
                        center.y + detailSize / 2,
                        detailSize
                );
                break;

            case GRASSLAND:
                graphics2D.setColor(
                        new Color(67, 112, 46)
                );

                graphics2D.drawLine(
                        center.x - 5,
                        center.y + 4,
                        center.x - 1,
                        center.y - detailSize
                );

                graphics2D.drawLine(
                        center.x + 5,
                        center.y + 4,
                        center.x + 1,
                        center.y - detailSize
                );
                break;

            case SEA:
                graphics2D.setColor(
                        new Color(172, 214, 235)
                );

                graphics2D.drawArc(
                        center.x - detailSize,
                        center.y - 3,
                        detailSize,
                        detailSize / 2,
                        0,
                        180
                );

                graphics2D.drawArc(
                        center.x,
                        center.y - 3,
                        detailSize,
                        detailSize / 2,
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
                        center.x - detailSize / 2,
                        center.y + detailSize / 2,
                        detailSize
                );

                drawMountainPeak(
                        graphics2D,
                        center.x + detailSize / 2,
                        center.y + detailSize / 2,
                        detailSize
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
            int baseY,
            int size
    ) {
        Polygon peak = new Polygon();

        peak.addPoint(
                centerX,
                baseY - size
        );

        peak.addPoint(
                centerX - size,
                baseY + size / 2
        );

        peak.addPoint(
                centerX + size,
                baseY + size / 2
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
                Math.max(4, (int) (6 * zoom)),
                Math.max(4, (int) (6 * zoom))
        );

        graphics2D.setColor(
                new Color(249, 231, 194)
        );

        graphics2D.setStroke(
                new BasicStroke(
                        (float) Math.max(
                                1.0,
                                2.0 * zoom
                        )
                )
        );

        graphics2D.drawRoundRect(
                x,
                y,
                width,
                height,
                Math.max(4, (int) (6 * zoom)),
                Math.max(4, (int) (6 * zoom))
        );

        graphics2D.setFont(
                new Font(
                        Font.SANS_SERIF,
                        Font.BOLD,
                        Math.max(
                                8,
                                (int) Math.round(11 * zoom)
                        )
                )
        );

        String text = "TH";

        int textWidth = graphics2D
                .getFontMetrics()
                .stringWidth(text);

        int textY =
                center.y +
                        graphics2D
                                .getFontMetrics()
                                .getAscent() / 2 -
                        2;

        graphics2D.drawString(
                text,
                center.x - textWidth / 2,
                textY
        );
    }

    private void drawNaturalResources(
            Graphics2D graphics2D,
            Set<NaturalResourceType> resources,
            Point center
    ) {
        if (resources.isEmpty()) {
            return;
        }

        double zoom = getZoomLevel();

        int badgeSize = Math.max(
                12,
                (int) Math.round(17.0 * zoom)
        );

        int gap = Math.max(
                2,
                (int) Math.round(2 * zoom)
        );

        int totalWidth =
                resources.size() * badgeSize +
                        (resources.size() - 1) * gap;

        int x = center.x - totalWidth / 2;

        int y =
                center.y +
                        (int) Math.round(10.0 * zoom);

        graphics2D.setFont(
                new Font(
                        Font.SANS_SERIF,
                        Font.BOLD,
                        Math.max(
                                7,
                                (int) Math.round(9 * zoom)
                        )
                )
        );

        for (NaturalResourceType resource : resources) {
            graphics2D.setColor(
                    getResourceColor(resource)
            );

            graphics2D.fillOval(
                    x,
                    y,
                    badgeSize,
                    badgeSize
            );

            graphics2D.setColor(Color.WHITE);

            String text =
                    getResourceAbbreviation(resource);

            int textWidth = graphics2D
                    .getFontMetrics()
                    .stringWidth(text);

            int textY =
                    y +
                            (
                                    badgeSize +
                                            graphics2D
                                                    .getFontMetrics()
                                                    .getAscent()
                            ) / 2 -
                            2;

            graphics2D.drawString(
                    text,
                    x + (badgeSize - textWidth) / 2,
                    textY
            );

            x += badgeSize + gap;
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
                        "Unknown natural resource"
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
                        "Unknown natural resource"
                );
        }
    }

    private void installMouseControls() {
        MouseAdapter mouseAdapter = new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent event) {
                if (SwingUtilities.isRightMouseButton(event) ||
                        SwingUtilities.isMiddleMouseButton(event)) {

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
                if (!panning || lastDragPoint == null) {
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
                if (SwingUtilities.isLeftMouseButton(event)) {
                    selectTileAt(event.getPoint());
                }
            }

            @Override
            public void mouseWheelMoved(
                    MouseWheelEvent event
            ) {
                changeZoom(event);
            }
        };

        addMouseListener(mouseAdapter);
        addMouseMotionListener(mouseAdapter);
        addMouseWheelListener(mouseAdapter);
    }

    private void selectTileAt(Point point) {
        for (HexTile tile : gameMap.getTiles()) {
            Polygon polygon = createHexPolygon(
                    getTileCenter(tile.getCoordinate())
            );

            if (polygon.contains(point)) {
                selectedTile = tile;
                selectionListener.accept(tile);
                repaint();
                return;
            }
        }
    }

    private void changeZoom(MouseWheelEvent event) {
        int direction = Integer.compare(
                event.getWheelRotation(),
                0
        );

        if (direction == 0) {
            return;
        }

        int newZoomIndex = Math.max(
                0,
                Math.min(
                        ZOOM_LEVELS.length - 1,
                        zoomIndex - direction
                )
        );

        if (newZoomIndex == zoomIndex) {
            return;
        }

        double oldZoom = getZoomLevel();

        double worldX =
                (
                        event.getX() -
                                getWidth() / 2.0 -
                                panX
                ) / oldZoom;

        double worldY =
                (
                        event.getY() -
                                getHeight() / 2.0 -
                                panY
                ) / oldZoom;

        zoomIndex = newZoomIndex;

        panX =
                event.getX() -
                        getWidth() / 2.0 -
                        worldX * getZoomLevel();

        panY =
                event.getY() -
                        getHeight() / 2.0 -
                        worldY * getZoomLevel();

        repaint();
    }
}