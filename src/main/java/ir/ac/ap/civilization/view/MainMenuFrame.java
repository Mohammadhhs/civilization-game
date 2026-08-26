package ir.ac.ap.civilization.view;

import ir.ac.ap.civilization.audio.BackgroundMusicPlayer;
import ir.ac.ap.civilization.controller.GameController;
import ir.ac.ap.civilization.model.game.GameState;
import ir.ac.ap.civilization.model.game.GameSaveService;
import ir.ac.ap.civilization.model.game.SaveSlot;
import ir.ac.ap.civilization.model.phase2.Difficulty;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.util.Objects;
import java.io.IOException;
import java.nio.file.Paths;

public final class MainMenuFrame extends JFrame {

    private static final long serialVersionUID = 1L;

    private final BackgroundMusicPlayer musicPlayer;

    public MainMenuFrame(
            BackgroundMusicPlayer musicPlayer
    ) {
        super("Civilization");

        this.musicPlayer = Objects.requireNonNull(
                musicPlayer,
                "Music player cannot be null"
        );

        configureFrame();
        setContentPane(createContent());
    }

    private void configureFrame() {
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(620, 600);
        setMinimumSize(new Dimension(520, 540));
        setLocationRelativeTo(null);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(
                    java.awt.event.WindowEvent event
            ) {
                requestExit();
            }
        });
    }

    private JPanel createContent() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(22, 27, 34));
        panel.setBorder(new EmptyBorder(55, 90, 55, 90));

        JLabel title = new JLabel(
                "CIVILIZATION",
                SwingConstants.CENTER
        );
        title.setAlignmentX(0.5f);
        title.setForeground(new Color(255, 214, 64));
        title.setFont(
                new Font(Font.SANS_SERIF, Font.BOLD, 38)
        );

        JLabel subtitle = new JLabel(
                "Build, explore and expand",
                SwingConstants.CENTER
        );
        subtitle.setAlignmentX(0.5f);
        subtitle.setForeground(new Color(174, 183, 194));
        subtitle.setFont(
                new Font(Font.SANS_SERIF, Font.PLAIN, 15)
        );

        JButton startButton = createButton(
                "Start",
                new Color(74, 170, 108)
        );
        JButton mapEditorButton = createButton(
                "Map Editor",
                new Color(151, 116, 196)
        );
        JButton settingsButton = createButton(
                "Settings",
                new Color(70, 145, 205)
        );
        JButton loadButton = createButton(
                "Load Game",
                new Color(227, 176, 64)
        );
        JButton exitButton = createButton(
                "Exit",
                new Color(176, 65, 62)
        );

        startButton.addActionListener(event -> startGame());
        mapEditorButton.addActionListener(event ->
                new MapEditorFrame(musicPlayer).setVisible(true));
        loadButton.addActionListener(event -> loadGame());
        settingsButton.addActionListener(
                event -> new SettingsDialog(
                        this,
                        musicPlayer
                ).setVisible(true)
        );
        exitButton.addActionListener(event -> requestExit());

        panel.add(title);
        panel.add(Box.createVerticalStrut(8));
        panel.add(subtitle);
        panel.add(Box.createVerticalGlue());
        panel.add(startButton);
        panel.add(Box.createVerticalStrut(14));
        panel.add(loadButton);
        panel.add(Box.createVerticalStrut(14));
        panel.add(mapEditorButton);
        panel.add(Box.createVerticalStrut(14));
        panel.add(settingsButton);
        panel.add(Box.createVerticalStrut(14));
        panel.add(exitButton);

        return panel;
    }

    private JButton createButton(
            String text,
            Color color
    ) {
        JButton button = new JButton(text);
        button.setAlignmentX(0.5f);
        button.setMaximumSize(new Dimension(320, 52));
        button.setPreferredSize(new Dimension(320, 52));
        button.setForeground(Color.BLACK);
        button.setBackground(color);
        button.setFocusPainted(false);
        button.setFont(
                new Font(Font.SANS_SERIF, Font.BOLD, 17)
        );
        button.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                color.brighter()
                        ),
                        new EmptyBorder(10, 20, 10, 20)
                )
        );
        return button;
    }

    private void startGame() {
        JComboBox<Difficulty> difficulty = new JComboBox<>(Difficulty.values());
        JTextField seed = new JTextField(
                String.valueOf(System.currentTimeMillis()), 16
        );
        JPanel options = new JPanel(new java.awt.GridLayout(2, 2, 8, 8));
        options.add(new JLabel("Difficulty:"));
        options.add(difficulty);
        options.add(new JLabel("Map seed:"));
        options.add(seed);
        if (JOptionPane.showConfirmDialog(
                this, options, "New Game",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        ) != JOptionPane.OK_OPTION) {
            return;
        }

        long mapSeed;
        try {
            mapSeed = Long.parseLong(seed.getText().trim());
        } catch (NumberFormatException exception) {
            JOptionPane.showMessageDialog(
                    this, "Seed must be a whole number",
                    "Invalid seed", JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        GameState gameState = new GameState(
                mapSeed,
                (Difficulty) difficulty.getSelectedItem()
        );
        GameController controller =
                new GameController(gameState);

        GameFrame gameFrame = new GameFrame(controller, musicPlayer);
        gameFrame.setVisible(true);
        dispose();
    }

    private void loadGame() {
        SaveSlot slot = (SaveSlot) JOptionPane.showInputDialog(
                this, "Choose save slot", "Load Game",
                JOptionPane.PLAIN_MESSAGE, null,
                SaveSlot.values(), SaveSlot.AUTOSAVE
        );
        if (slot == null) return;
        GameSaveService saves = new GameSaveService(
                Paths.get(System.getProperty("user.home"),
                        ".civilization", "saves")
        );
        try {
            GameState loaded = saves.load(slot);
            new GameFrame(new GameController(loaded), musicPlayer).setVisible(true);
            dispose();
        } catch (IOException exception) {
            JOptionPane.showMessageDialog(this, exception.getMessage(),
                    "Load failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void requestExit() {
        int answer = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to exit?",
                "Exit game",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (answer == JOptionPane.YES_OPTION) {
            musicPlayer.close();
            dispose();
            System.exit(0);
        }
    }
}
