package ir.ac.ap;

import ir.ac.ap.civilization.controller.GameController;
import ir.ac.ap.civilization.model.game.GameState;
import ir.ac.ap.civilization.view.GameFrame;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            applySystemLookAndFeel();

            GameState gameState = new GameState();

            GameController controller =
                    new GameController(gameState);

            GameFrame gameFrame =
                    new GameFrame(controller);

            gameFrame.setVisible(true);
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