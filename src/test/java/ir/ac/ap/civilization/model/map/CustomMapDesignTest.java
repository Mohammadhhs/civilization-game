package ir.ac.ap.civilization.model.map;

import ir.ac.ap.civilization.model.game.GameState;
import ir.ac.ap.civilization.model.phase2.Difficulty;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CustomMapDesignTest {

    @Test
    void designBuildsCompletePlayableMap() {
        CustomMapDesign design = new CustomMapDesign(4, 9042L);
        HexCoordinate edited = new HexCoordinate(2, -1);
        design.paint(edited, TerrainType.GRASSLAND, NaturalResourceType.CATTLE);

        GameMap map = design.toGameMap();

        assertEquals(GameMap.calculateTileCount(4), map.size());
        assertEquals(TerrainType.GRASSLAND,
                map.getTile(edited).get().getTerrainType());
        assertTrue(map.getTile(edited).get()
                .hasNaturalResource(NaturalResourceType.CATTLE));
        GameState state = new GameState(map, Difficulty.NORMAL);
        assertEquals(5, state.getUnits().size());
    }

    @Test
    void editorProtectsStartingUnitArea() {
        CustomMapDesign design = new CustomMapDesign(4, 12L);
        assertThrows(IllegalArgumentException.class, () -> design.paint(
                new HexCoordinate(1, 0), TerrainType.SEA, null));
    }

    @Test
    void customMapRoundTripsThroughFile(@TempDir Path directory) throws Exception {
        CustomMapDesign design = new CustomMapDesign(3, 77L);
        HexCoordinate coordinate = new HexCoordinate(2, -1);
        design.paint(coordinate, TerrainType.MOUNTAIN, NaturalResourceType.IRON);
        Path file = directory.resolve("evaluation.civmap");

        CustomMapFileService service = new CustomMapFileService();
        service.save(design, file);
        CustomMapDesign loaded = service.load(file);

        assertEquals(3, loaded.getRadius());
        assertEquals(77L, loaded.getSeed());
        assertEquals(TerrainType.MOUNTAIN, loaded.getTerrain(coordinate));
        assertEquals(NaturalResourceType.IRON, loaded.getResource(coordinate));
    }
}
