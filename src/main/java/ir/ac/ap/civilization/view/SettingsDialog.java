package ir.ac.ap.civilization.view;

import ir.ac.ap.civilization.audio.BackgroundMusicPlayer;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.util.Objects;

public final class SettingsDialog extends JDialog {

    private static final long serialVersionUID = 1L;

    public SettingsDialog(
            JFrame owner,
            BackgroundMusicPlayer musicPlayer
    ) {
        super(owner, "Settings", true);

        Objects.requireNonNull(
                musicPlayer,
                "Music player cannot be null"
        );

        JPanel content = new JPanel(new BorderLayout(10, 14));
        content.setBackground(new Color(30, 35, 43));
        content.setBorder(new EmptyBorder(18, 18, 18, 18));

        JLabel title = new JLabel(
                "Background music volume",
                SwingConstants.CENTER
        );
        title.setForeground(new Color(235, 239, 244));
        title.setFont(
                new Font(Font.SANS_SERIF, Font.BOLD, 16)
        );

        JSlider volumeSlider = new JSlider(
                0,
                100,
                musicPlayer.getVolumePercent()
        );
        volumeSlider.setMajorTickSpacing(25);
        volumeSlider.setPaintTicks(true);
        volumeSlider.setPaintLabels(true);
        volumeSlider.setBackground(new Color(30, 35, 43));
        volumeSlider.setForeground(new Color(235, 239, 244));
        volumeSlider.addChangeListener(
                event -> musicPlayer.setVolumePercent(
                        volumeSlider.getValue()
                )
        );

        JButton closeButton = new JButton("Close");
        closeButton.setFocusPainted(false);
        closeButton.setBackground(new Color(74, 170, 108));
        closeButton.setForeground(Color.WHITE);
        closeButton.addActionListener(event -> dispose());

        content.add(title, BorderLayout.NORTH);
        content.add(volumeSlider, BorderLayout.CENTER);
        content.add(closeButton, BorderLayout.SOUTH);

        setContentPane(content);
        getRootPane().setBorder(
                BorderFactory.createLineBorder(
                        new Color(62, 71, 83)
                )
        );
        setPreferredSize(new Dimension(420, 220));
        pack();
        setResizable(false);
        setLocationRelativeTo(owner);
    }
}
