package ir.ac.ap;

import ir.ac.ap.civilization.audio.BackgroundMusicPlayer;
import ir.ac.ap.civilization.view.MainMenuFrame;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            applySystemLookAndFeel();

            BackgroundMusicPlayer musicPlayer =
                    new BackgroundMusicPlayer();

            musicPlayer.start();

            Runtime.getRuntime().addShutdownHook(
                    new Thread(musicPlayer::close)
            );

            new MainMenuFrame(musicPlayer).setVisible(true);
        });
    }

    private static void applySystemLookAndFeel() {
        try {
            UIManager.setLookAndFeel(
                    UIManager.getSystemLookAndFeelClassName()
            );
        } catch (Exception ignored) {
            // Swing will use its default look and feel.
        }
    }
}
