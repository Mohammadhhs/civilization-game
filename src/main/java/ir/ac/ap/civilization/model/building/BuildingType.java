package ir.ac.ap.civilization.model.building;

import ir.ac.ap.civilization.model.map.NaturalResourceType;
import ir.ac.ap.civilization.model.resource.ResourceType;
import ir.ac.ap.civilization.model.technology.TechnologyType;

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public enum BuildingType {

    LUMBER_MILL(
            "Lumber Mill",
            "LM",
            ResourceType.WOOD,
            EnumSet.of(NaturalResourceType.WOOD),
            4,
            2,
            1,
            createCost(15, 0, 0),
            createCost(1, 0, 0),
            null
    ),

    FARM(
            "Farm",
            "F",
            ResourceType.FOOD,
            EnumSet.of(
                    NaturalResourceType.WHEAT,
                    NaturalResourceType.RICE
            ),
            5,
            2,
            1,
            createCost(20, 0, 0),
            createCost(1, 0, 0),
            null
    ),

    STABLE(
            "Stable",
            "ST",
            ResourceType.FOOD,
            EnumSet.of(
                    NaturalResourceType.CATTLE,
                    NaturalResourceType.SHEEP
            ),
            4,
            2,
            1,
            createCost(25, 0, 0),
            createCost(1, 0, 0),
            null
    ),

    STONE_MINE(
            "Stone Mine",
            "SM",
            ResourceType.STONE,
            EnumSet.of(NaturalResourceType.STONE),
            4,
            2,
            2,
            createCost(25, 0, 0),
            createCost(1, 0, 0),
            TechnologyType.STONE_MINING
    ),

    IRON_MINE(
            "Iron Mine",
            "IM",
            ResourceType.IRON,
            EnumSet.of(NaturalResourceType.IRON),
            2,
            2,
            2,
            createCost(30, 20, 0),
            createCost(0, 1, 0),
            TechnologyType.IRON_MINING
    );

    private final String displayName;
    private final String mapSymbol;
    private final ResourceType producedResource;
    private final Set<NaturalResourceType>
            compatibleNaturalResources;
    private final int baseProductionPerWorker;
    private final int workerCapacity;
    private final int constructionActionPointCost;
    private final Map<ResourceType, Integer> constructionCost;
    private final Map<ResourceType, Integer> upkeepCost;
    private final TechnologyType requiredTechnology;

    BuildingType(
            String displayName,
            String mapSymbol,
            ResourceType producedResource,
            Set<NaturalResourceType>
                    compatibleNaturalResources,
            int baseProductionPerWorker,
            int workerCapacity,
            int constructionActionPointCost,
            Map<ResourceType, Integer> constructionCost,
            Map<ResourceType, Integer> upkeepCost,
            TechnologyType requiredTechnology
    ) {
        this.displayName = displayName;
        this.mapSymbol = mapSymbol;
        this.producedResource = producedResource;
        this.compatibleNaturalResources =
                Collections.unmodifiableSet(
                        EnumSet.copyOf(
                                compatibleNaturalResources
                        )
                );
        this.baseProductionPerWorker =
                baseProductionPerWorker;
        this.workerCapacity = workerCapacity;
        this.constructionActionPointCost =
                constructionActionPointCost;
        this.constructionCost = constructionCost;
        this.upkeepCost = upkeepCost;
        this.requiredTechnology = requiredTechnology;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getMapSymbol() {
        return mapSymbol;
    }

    public ResourceType getProducedResource() {
        return producedResource;
    }

    public Set<NaturalResourceType>
    getCompatibleNaturalResources() {
        return compatibleNaturalResources;
    }

    public boolean supportsNaturalResource(
            NaturalResourceType naturalResourceType
    ) {
        return compatibleNaturalResources.contains(
                naturalResourceType
        );
    }

    public int getBaseProductionPerWorker() {
        return baseProductionPerWorker;
    }

    public int getWorkerCapacity() {
        return workerCapacity;
    }

    public int getConstructionActionPointCost() {
        return constructionActionPointCost;
    }

    public Map<ResourceType, Integer> getConstructionCost() {
        return constructionCost;
    }

    public Map<ResourceType, Integer> getUpkeepCost() {
        return upkeepCost;
    }

    public Optional<TechnologyType> getRequiredTechnology() {
        return Optional.ofNullable(requiredTechnology);
    }

    @Override
    public String toString() {
        return displayName;
    }

    private static Map<ResourceType, Integer> createCost(
            int wood,
            int stone,
            int iron
    ) {
        EnumMap<ResourceType, Integer> cost =
                new EnumMap<>(ResourceType.class);

        if (wood > 0) {
            cost.put(ResourceType.WOOD, wood);
        }

        if (stone > 0) {
            cost.put(ResourceType.STONE, stone);
        }

        if (iron > 0) {
            cost.put(ResourceType.IRON, iron);
        }

        return Collections.unmodifiableMap(cost);
    }
}
