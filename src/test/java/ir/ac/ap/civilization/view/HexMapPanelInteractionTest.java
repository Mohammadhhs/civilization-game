package ir.ac.ap.civilization.view;

import ir.ac.ap.civilization.controller.GameController;
import ir.ac.ap.civilization.model.game.GameState;
import ir.ac.ap.civilization.model.unit.Unit;
import org.junit.jupiter.api.Test;

import javax.swing.SwingUtilities;
import java.awt.event.MouseEvent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class HexMapPanelInteractionTest {

    private static final int PANEL_WIDTH = 1280;
    private static final int PANEL_HEIGHT = 800;
    private static final double DEFAULT_HEX_RADIUS = 90.0;

    @Test
    void clickingFreeSpaceMovesIntoAnOccupiedHex()
            throws Exception {
        GameState gameState = new GameState();
        GameController controller =
                new GameController(gameState);

        Unit firstBuilder = gameState.getUnitById(1)
                .orElseThrow(AssertionError::new);
        Unit secondBuilder = gameState.getUnitById(2)
                .orElseThrow(AssertionError::new);

        controller.moveUnit(
                firstBuilder.getId(),
                gameState.getTownHallCoordinate()
        );

        HexMapPanel[] panelHolder = new HexMapPanel[1];

        SwingUtilities.invokeAndWait(() -> {
            HexMapPanel panel = new HexMapPanel(controller);
            panel.setSize(PANEL_WIDTH, PANEL_HEIGHT);
            panelHolder[0] = panel;

            int secondBuilderX = (int) Math.round(
                    PANEL_WIDTH / 2.0 +
                            Math.sqrt(3.0) *
                                    DEFAULT_HEX_RADIUS * 0.5
            );
            int secondBuilderY = (int) Math.round(
                    PANEL_HEIGHT / 2.0 -
                            DEFAULT_HEX_RADIUS * 1.5
            );

            click(panel, secondBuilderX, secondBuilderY);

            /*
             * The first Builder sprite occupies the Town Hall center.
             * Clicking lower, still inside the hex, must mean movement.
             */
            click(
                    panel,
                    PANEL_WIDTH / 2,
                    PANEL_HEIGHT / 2 + 65
            );
        });

        HexMapPanel panel = panelHolder[0];
        assertNotNull(panel.getSelectedUnit());
        assertEquals(
                secondBuilder.getId(),
                panel.getSelectedUnit().getId()
        );
        assertEquals(
                2,
                gameState.getUnitsAt(
                        gameState.getTownHallCoordinate()
                ).size()
        );
    }

    private static void click(
            HexMapPanel panel,
            int x,
            int y
    ) {
        panel.dispatchEvent(
                new MouseEvent(
                        panel,
                        MouseEvent.MOUSE_CLICKED,
                        System.currentTimeMillis(),
                        0,
                        x,
                        y,
                        1,
                        false,
                        MouseEvent.BUTTON1
                )
        );
    }
}
