package ir.ac.ap.civilization.view.asset;

import ir.ac.ap.civilization.model.building.BuildingType;
import ir.ac.ap.civilization.model.building.TownHallLevel;
import ir.ac.ap.civilization.model.map.NaturalResourceType;
import ir.ac.ap.civilization.model.map.TerrainType;
import ir.ac.ap.civilization.model.unit.UnitType;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class GameImageRepositoryTest {

    @Test
    void loadsAllTwentySevenImagesFromTheClasspath() throws IOException {
        GameImageRepository repository = new GameImageRepository(
                new PngClassLoader(createPngBytes())
        );

        assertEquals(27, repository.getRequiredImageCount());
        assertEquals(27, repository.getLoadedRequiredImageCount());
        assertEquals(0, repository.getMissingRequiredPaths().size());

        for (TerrainType type : TerrainType.values()) {
            assertNotNull(repository.getTerrainImage(type));
        }
        for (NaturalResourceType type : NaturalResourceType.values()) {
            assertNotNull(repository.getNaturalResourceImage(type));
        }
        for (BuildingType type : BuildingType.values()) {
            assertNotNull(repository.getBuildingImage(type));
        }
        for (TownHallLevel level : TownHallLevel.values()) {
            assertNotNull(repository.getTownHallImage(level));
        }
        for (UnitType type : UnitType.values()) {
            assertNotNull(repository.getUnitImage(type));
        }
        assertNotNull(repository.getPopulationSettlementImage());
    }

    @Test
    void missingImagesRemainSafeForVectorFallback() {
        GameImageRepository repository = new GameImageRepository(
                new ClassLoader(null) {
                    @Override
                    public InputStream getResourceAsStream(String name) {
                        return null;
                    }
                }
        );

        assertEquals(0, repository.getLoadedRequiredImageCount());
        assertEquals(27, repository.getMissingRequiredPaths().size());
        assertNull(repository.getTerrainImage(TerrainType.FOREST));
        assertNull(repository.getUnitImage(UnitType.BUILDER));
    }

    private byte[] createPngBytes() throws IOException {
        BufferedImage image = new BufferedImage(
                4,
                4,
                BufferedImage.TYPE_INT_ARGB
        );
        image.setRGB(1, 1, Color.GREEN.getRGB());

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(image, "png", output);
        return output.toByteArray();
    }

    private static final class PngClassLoader extends ClassLoader {

        private final byte[] pngBytes;

        private PngClassLoader(byte[] pngBytes) {
            super(null);
            this.pngBytes = pngBytes;
        }

        @Override
        public InputStream getResourceAsStream(String name) {
            return name.startsWith("images/map/")
                    ? new ByteArrayInputStream(pngBytes)
                    : null;
        }
    }
}
