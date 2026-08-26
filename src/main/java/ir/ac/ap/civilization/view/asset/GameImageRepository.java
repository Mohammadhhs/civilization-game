package ir.ac.ap.civilization.view.asset;

import ir.ac.ap.civilization.model.building.BuildingType;
import ir.ac.ap.civilization.model.building.TownHallLevel;
import ir.ac.ap.civilization.model.map.NaturalResourceType;
import ir.ac.ap.civilization.model.map.TerrainType;
import ir.ac.ap.civilization.model.resource.ResourceType;
import ir.ac.ap.civilization.model.unit.UnitType;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Loads every game image once and keeps the Model independent from file paths.
 * A missing or unreadable image is represented by null so the View can use its
 * vector fallback without preventing the game from starting.
 */
public final class GameImageRepository {

    public static final int REQUIRED_IMAGE_COUNT = 27;

    private static final String ROOT = "/images/map/";

    private final Map<TerrainType, BufferedImage> terrainImages;
    private final Map<NaturalResourceType, BufferedImage> resourceImages;
    private final Map<BuildingType, BufferedImage> buildingImages;
    private final Map<TownHallLevel, BufferedImage> townHallImages;
    private final Map<UnitType, BufferedImage> unitImages;
    private final List<String> missingRequiredPaths;
    private final BufferedImage populationSettlementImage;

    public GameImageRepository() {
        this(GameImageRepository.class.getClassLoader());
    }

    GameImageRepository(ClassLoader classLoader) {
        Objects.requireNonNull(classLoader, "Class loader cannot be null");

        terrainImages = new EnumMap<>(TerrainType.class);
        resourceImages = new EnumMap<>(NaturalResourceType.class);
        buildingImages = new EnumMap<>(BuildingType.class);
        townHallImages = new EnumMap<>(TownHallLevel.class);
        unitImages = new EnumMap<>(UnitType.class);
        missingRequiredPaths = new ArrayList<>();

        put(terrainImages, TerrainType.FOREST,
                ROOT + "terrain/forest.png", classLoader);
        put(terrainImages, TerrainType.PLAIN,
                ROOT + "terrain/plain.png", classLoader);
        put(terrainImages, TerrainType.MOUNTAIN,
                ROOT + "terrain/mountain.png", classLoader);
        put(terrainImages, TerrainType.GRASSLAND,
                ROOT + "terrain/grassland.png", classLoader);
        put(terrainImages, TerrainType.SEA,
                ROOT + "terrain/sea.png", classLoader);
        put(terrainImages, TerrainType.MOUNTAIN_RANGE,
                ROOT + "terrain/mountain_range.png", classLoader);

        put(resourceImages, NaturalResourceType.WOOD,
                ROOT + "natural_resources/wood.png", classLoader);
        put(resourceImages, NaturalResourceType.STONE,
                ROOT + "natural_resources/stone.png", classLoader);
        put(resourceImages, NaturalResourceType.IRON,
                ROOT + "natural_resources/iron.png", classLoader);
        put(resourceImages, NaturalResourceType.WHEAT,
                ROOT + "natural_resources/wheat.png", classLoader);
        put(resourceImages, NaturalResourceType.RICE,
                ROOT + "natural_resources/rice.png", classLoader);
        put(resourceImages, NaturalResourceType.CATTLE,
                ROOT + "natural_resources/cattle.png", classLoader);
        put(resourceImages, NaturalResourceType.SHEEP,
                ROOT + "natural_resources/sheep.png", classLoader);
        put(resourceImages, NaturalResourceType.FISH,
                ROOT + "natural_resources/fish.png", classLoader);

        put(townHallImages, TownHallLevel.BASE_CAMP,
                ROOT + "buildings/town_hall/base_camp.png", classLoader);
        put(townHallImages, TownHallLevel.SETTLEMENT,
                ROOT + "buildings/town_hall/military_settlement.png",
                classLoader);
        put(townHallImages, TownHallLevel.CAPITAL,
                ROOT + "buildings/town_hall/capital.png", classLoader);

        put(buildingImages, BuildingType.LUMBER_MILL,
                ROOT + "buildings/lumber_mill.png", classLoader);
        put(buildingImages, BuildingType.FARM,
                ROOT + "buildings/farm.png", classLoader);
        put(buildingImages, BuildingType.STABLE,
                ROOT + "buildings/stable.png", classLoader);
        put(buildingImages, BuildingType.STONE_MINE,
                ROOT + "buildings/stone_mine.png", classLoader);
        put(buildingImages, BuildingType.IRON_MINE,
                ROOT + "buildings/iron_mine.png", classLoader);

        populationSettlementImage = load(
                ROOT + "buildings/population_settlement.png", classLoader);

        put(unitImages, UnitType.BUILDER,
                ROOT + "units/civilian/builder.png", classLoader);
        put(unitImages, UnitType.WORKER,
                ROOT + "units/civilian/worker.png", classLoader);
        put(unitImages, UnitType.EXPLORER,
                ROOT + "units/civilian/explorer.png", classLoader);
        put(unitImages, UnitType.BORDER_EXPANDER,
                ROOT + "units/civilian/border_expander.png", classLoader);
        putOptional(unitImages, UnitType.SWORDSMAN,
                ROOT + "units/military/swordsman.png", classLoader);
        putOptional(unitImages, UnitType.ARCHER,
                ROOT + "units/military/archer.png", classLoader);
        putOptional(unitImages, UnitType.CAVALRY,
                ROOT + "units/military/cavalry.png", classLoader);
        putOptional(unitImages, UnitType.CATAPULT,
                ROOT + "units/military/catapult.png", classLoader);
    }

    public BufferedImage getTerrainImage(TerrainType terrainType) {
        return terrainImages.get(terrainType);
    }

    public BufferedImage getNaturalResourceImage(
            NaturalResourceType resourceType
    ) {
        return resourceImages.get(resourceType);
    }

    public BufferedImage getBuildingImage(BuildingType buildingType) {
        return buildingImages.get(buildingType);
    }

    public BufferedImage getTownHallImage(TownHallLevel level) {
        return townHallImages.get(level);
    }

    public BufferedImage getUnitImage(UnitType unitType) {
        return unitImages.get(unitType);
    }

    public BufferedImage getPopulationSettlementImage() {
        return populationSettlementImage;
    }

    public BufferedImage getStockpileImage(ResourceType resourceType) {
        switch (resourceType) {
            case FOOD:
                return getNaturalResourceImage(NaturalResourceType.WHEAT);
            case WOOD:
                return getNaturalResourceImage(NaturalResourceType.WOOD);
            case STONE:
                return getNaturalResourceImage(NaturalResourceType.STONE);
            case IRON:
                return getNaturalResourceImage(NaturalResourceType.IRON);
            default:
                throw new IllegalStateException("Unknown resource type");
        }
    }

    public List<String> getMissingRequiredPaths() {
        return Collections.unmodifiableList(
                new ArrayList<>(missingRequiredPaths)
        );
    }

    public int getRequiredImageCount() {
        return REQUIRED_IMAGE_COUNT;
    }

    public int getLoadedRequiredImageCount() {
        return REQUIRED_IMAGE_COUNT - missingRequiredPaths.size();
    }

    private <T extends Enum<T>> void put(
            Map<T, BufferedImage> target,
            T key,
            String path,
            ClassLoader classLoader
    ) {
        target.put(key, load(path, classLoader));
    }

    private <T extends Enum<T>> void putOptional(
            Map<T, BufferedImage> target,
            T key,
            String path,
            ClassLoader classLoader
    ) {
        target.put(key, loadOptional(path, classLoader));
    }

    private BufferedImage loadOptional(String path, ClassLoader classLoader) {
        String classPath = path.startsWith("/") ? path.substring(1) : path;
        try (InputStream stream = classLoader.getResourceAsStream(classPath)) {
            return stream == null ? null : ImageIO.read(stream);
        } catch (IOException exception) {
            return null;
        }
    }

    private BufferedImage load(String path, ClassLoader classLoader) {
        String classPath = path.startsWith("/")
                ? path.substring(1)
                : path;

        try (InputStream stream =
                     classLoader.getResourceAsStream(classPath)) {
            if (stream == null) {
                missingRequiredPaths.add(path);
                return null;
            }

            BufferedImage image = ImageIO.read(stream);
            if (image == null) {
                missingRequiredPaths.add(path);
            }
            return image;
        } catch (IOException exception) {
            missingRequiredPaths.add(path);
            return null;
        }
    }
}
