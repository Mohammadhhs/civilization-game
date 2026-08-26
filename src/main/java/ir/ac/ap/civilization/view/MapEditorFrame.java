package ir.ac.ap.civilization.view;

import ir.ac.ap.civilization.audio.BackgroundMusicPlayer;
import ir.ac.ap.civilization.controller.GameController;
import ir.ac.ap.civilization.model.game.GameState;
import ir.ac.ap.civilization.model.map.CustomMapDesign;
import ir.ac.ap.civilization.model.map.CustomMapFileService;
import ir.ac.ap.civilization.model.map.HexCoordinate;
import ir.ac.ap.civilization.model.map.NaturalResourceType;
import ir.ac.ap.civilization.model.map.TerrainType;
import ir.ac.ap.civilization.model.phase2.Difficulty;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public final class MapEditorFrame extends JFrame {

    private static final long serialVersionUID = 1L;
    private final BackgroundMusicPlayer musicPlayer;
    private final CustomMapFileService files = new CustomMapFileService();
    private final JComboBox<TerrainType> terrain = new JComboBox<>(TerrainType.values());
    private final JComboBox<String> resource = new JComboBox<>();
    private final JLabel selection = new JLabel("Click a hex to paint it");
    private CustomMapDesign design;
    private EditorCanvas canvas;

    public MapEditorFrame(BackgroundMusicPlayer musicPlayer) {
        super("Civilization Map Editor");
        this.musicPlayer = musicPlayer;
        for (String item : resourceChoices()) resource.addItem(item);
        this.design = new CustomMapDesign(5, System.currentTimeMillis());
        configure();
    }

    private void configure() {
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1180, 820);
        setMinimumSize(new Dimension(900, 650));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));
        canvas = new EditorCanvas();
        add(canvas, BorderLayout.CENTER);
        add(createTools(), BorderLayout.WEST);
    }

    private JPanel createTools() {
        JPanel tools = new JPanel(new java.awt.GridLayout(0, 1, 8, 8));
        tools.setPreferredSize(new Dimension(250, 0));
        tools.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        JLabel title = new JLabel("MAP EDITOR");
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
        tools.add(title);
        tools.add(selection);
        tools.add(new JLabel("Terrain brush"));
        tools.add(terrain);
        tools.add(new JLabel("Resource brush"));
        tools.add(resource);

        JButton newMap = new JButton("New Map");
        JButton save = new JButton("Save .civmap");
        JButton load = new JButton("Load .civmap");
        JButton play = new JButton("Play This Map");
        for (JButton button : new JButton[]{newMap, save, load, play}) {
            button.setForeground(Color.BLACK);
        }
        newMap.addActionListener(event -> createNewDesign());
        save.addActionListener(event -> saveDesign());
        load.addActionListener(event -> loadDesign());
        play.addActionListener(event -> playDesign());
        tools.add(newMap);
        tools.add(save);
        tools.add(load);
        tools.add(play);
        return tools;
    }

    private void createNewDesign() {
        JSpinner radius = new JSpinner(new SpinnerNumberModel(5, 2, 12, 1));
        if (JOptionPane.showConfirmDialog(this, radius,
                "Choose map radius (2-12)", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) return;
        design = new CustomMapDesign((Integer) radius.getValue(), System.currentTimeMillis());
        selection.setText("New map ready");
        canvas.repaint();
    }

    private void saveDesign() {
        JFileChooser chooser = chooser();
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        Path path = chooser.getSelectedFile().toPath();
        if (!path.getFileName().toString().toLowerCase().endsWith(".civmap")) {
            path = path.resolveSibling(path.getFileName() + ".civmap");
        }
        try {
            files.save(design, path);
            selection.setText("Saved: " + path.getFileName());
        } catch (IOException exception) {
            showError(exception);
        }
    }

    private void loadDesign() {
        JFileChooser chooser = chooser();
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        try {
            design = files.load(chooser.getSelectedFile().toPath());
            selection.setText("Loaded: " + chooser.getSelectedFile().getName());
            canvas.repaint();
        } catch (IOException exception) {
            showError(exception);
        }
    }

    private void playDesign() {
        Difficulty difficulty = (Difficulty) JOptionPane.showInputDialog(
                this, "Difficulty", "Play Custom Map",
                JOptionPane.PLAIN_MESSAGE, null,
                Difficulty.values(), Difficulty.NORMAL
        );
        if (difficulty == null) return;
        GameState state = new GameState(design.toGameMap(), difficulty);
        new GameFrame(new GameController(state), musicPlayer).setVisible(true);
        dispose();
    }

    private JFileChooser chooser() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter(
                "Civilization maps (*.civmap)", "civmap"));
        return chooser;
    }

    private void showError(Exception exception) {
        JOptionPane.showMessageDialog(this, exception.getMessage(),
                "Map editor error", JOptionPane.ERROR_MESSAGE);
    }

    private String[] resourceChoices() {
        NaturalResourceType[] values = NaturalResourceType.values();
        String[] choices = new String[values.length + 1];
        choices[0] = "NONE";
        for (int index = 0; index < values.length; index++) {
            choices[index + 1] = values[index].name();
        }
        return choices;
    }

    private final class EditorCanvas extends JPanel {
        private final Map<HexCoordinate, Polygon> shapes = new LinkedHashMap<>();

        private EditorCanvas() {
            setBackground(new Color(28, 34, 42));
            addMouseListener(new MouseAdapter() {
                @Override public void mousePressed(MouseEvent event) {
                    paintAt(event.getX(), event.getY());
                }
            });
            addMouseMotionListener(new MouseAdapter() {
                @Override public void mouseDragged(MouseEvent event) {
                    paintAt(event.getX(), event.getY());
                }
            });
        }

        @Override protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g = (Graphics2D) graphics.create();
            try {
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                shapes.clear();
                double size = Math.max(20.0, Math.min(48.0,
                        Math.min(getWidth() / (3.2 * design.getRadius() + 3.0),
                                getHeight() / (3.0 * design.getRadius() + 3.0))));
                for (HexCoordinate coordinate : design.getCoordinates()) {
                    double centerX = getWidth() / 2.0 + size * 1.5 * coordinate.getQ();
                    double centerY = getHeight() / 2.0 + size * Math.sqrt(3.0) *
                            (coordinate.getR() + coordinate.getQ() / 2.0);
                    Polygon polygon = polygon(centerX, centerY, size);
                    shapes.put(coordinate, polygon);
                    g.setColor(color(design.getTerrain(coordinate)));
                    g.fillPolygon(polygon);
                    g.setColor(new Color(12, 15, 18));
                    g.setStroke(new BasicStroke(1.6f));
                    g.drawPolygon(polygon);
                    NaturalResourceType value = design.getResource(coordinate);
                    if (value != null) {
                        g.setColor(Color.BLACK);
                        g.setFont(g.getFont().deriveFont(Font.BOLD, (float) Math.max(9, size / 3)));
                        String symbol = value.name().substring(0, 1);
                        g.drawString(symbol,
                                (float) (centerX - g.getFontMetrics().stringWidth(symbol) / 2.0),
                                (float) (centerY + g.getFontMetrics().getAscent() / 3.0));
                    }
                }
            } finally {
                g.dispose();
            }
        }

        private void paintAt(int x, int y) {
            for (Map.Entry<HexCoordinate, Polygon> entry : shapes.entrySet()) {
                if (!entry.getValue().contains(x, y)) continue;
                NaturalResourceType resourceType = "NONE".equals(resource.getSelectedItem())
                        ? null : NaturalResourceType.valueOf((String) resource.getSelectedItem());
                try {
                    design.paint(entry.getKey(),
                            (TerrainType) terrain.getSelectedItem(), resourceType);
                    selection.setText("Selected: " + entry.getKey());
                    repaint();
                } catch (IllegalArgumentException exception) {
                    selection.setText(exception.getMessage());
                }
                return;
            }
        }

        private Polygon polygon(double centerX, double centerY, double size) {
            Polygon result = new Polygon();
            for (int index = 0; index < 6; index++) {
                double angle = Math.toRadians(60 * index);
                result.addPoint((int) Math.round(centerX + size * Math.cos(angle)),
                        (int) Math.round(centerY + size * Math.sin(angle)));
            }
            return result;
        }

        private Color color(TerrainType value) {
            switch (value) {
                case FOREST: return new Color(52, 112, 72);
                case PLAIN: return new Color(205, 187, 120);
                case GRASSLAND: return new Color(116, 168, 86);
                case MOUNTAIN: return new Color(145, 136, 126);
                case SEA: return new Color(57, 128, 177);
                default: return new Color(91, 88, 91);
            }
        }
    }
}
