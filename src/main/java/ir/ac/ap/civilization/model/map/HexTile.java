package ir.ac.ap.civilization.model.map;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public final class HexTile {

    private static final int DEFAULT_DEPOSIT_AMOUNT = 100;

    private final HexCoordinate coordinate;
    private final TerrainType terrainType;
    private final EnumMap<
            NaturalResourceType,
            NaturalResourceDeposit
            > resourceDeposits;

    private boolean discovered;
    private boolean visible;
    private boolean insidePlayerTerritory;

    public HexTile(
            HexCoordinate coordinate,
            TerrainType terrainType
    ) {
        this(
                coordinate,
                terrainType,
                Collections
                        .<NaturalResourceType, Integer>emptyMap()
        );
    }

    public HexTile(
            HexCoordinate coordinate,
            TerrainType terrainType,
            Set<NaturalResourceType> naturalResources
    ) {
        this(
                coordinate,
                terrainType,
                createDefaultResourceAmounts(
                        naturalResources
                )
        );
    }

    private HexTile(
            HexCoordinate coordinate,
            TerrainType terrainType,
            Map<NaturalResourceType, Integer>
                    resourceAmounts
    ) {
        this.coordinate = Objects.requireNonNull(
                coordinate,
                "Coordinate cannot be null"
        );

        this.terrainType = Objects.requireNonNull(
                terrainType,
                "Terrain type cannot be null"
        );

        Objects.requireNonNull(
                resourceAmounts,
                "Natural resource amounts cannot be null"
        );

        this.resourceDeposits =
                new EnumMap<>(NaturalResourceType.class);

        copyResourceAmounts(resourceAmounts);
    }

    public static HexTile withResourceAmounts(
            HexCoordinate coordinate,
            TerrainType terrainType,
            Map<NaturalResourceType, Integer>
                    resourceAmounts
    ) {
        return new HexTile(
                coordinate,
                terrainType,
                resourceAmounts
        );
    }

    public HexCoordinate getCoordinate() {
        return coordinate;
    }

    public TerrainType getTerrainType() {
        return terrainType;
    }

    public Set<NaturalResourceType> getNaturalResources() {
        return Collections.unmodifiableSet(
                resourceDeposits.keySet()
        );
    }

    public boolean hasNaturalResource(
            NaturalResourceType resourceType
    ) {
        Objects.requireNonNull(
                resourceType,
                "Natural resource type cannot be null"
        );

        return resourceDeposits.containsKey(resourceType);
    }

    public boolean hasAvailableNaturalResource(
            NaturalResourceType resourceType
    ) {
        return getNaturalResourceDeposit(resourceType)
                .map(deposit -> !deposit.isDepleted())
                .orElse(false);
    }

    public Optional<NaturalResourceDeposit>
    getNaturalResourceDeposit(
            NaturalResourceType resourceType
    ) {
        Objects.requireNonNull(
                resourceType,
                "Natural resource type cannot be null"
        );

        return Optional.ofNullable(
                resourceDeposits.get(resourceType)
        );
    }

    public int getInitialResourceAmount(
            NaturalResourceType resourceType
    ) {
        return getRequiredDeposit(resourceType)
                .getInitialAmount();
    }

    public int getRemainingResourceAmount(
            NaturalResourceType resourceType
    ) {
        return getRequiredDeposit(resourceType)
                .getRemainingAmount();
    }

    public boolean isNaturalResourceDepleted(
            NaturalResourceType resourceType
    ) {
        return getRequiredDeposit(resourceType)
                .isDepleted();
    }

    public int extractNaturalResource(
            NaturalResourceType resourceType,
            int requestedAmount
    ) {
        return getRequiredDeposit(resourceType)
                .extract(requestedAmount);
    }

    public boolean isDiscovered() {
        return discovered;
    }

    public boolean isVisible() {
        return visible;
    }

    public boolean isInsidePlayerTerritory() {
        return insidePlayerTerritory;
    }

    public void discover() {
        discovered = true;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;

        if (visible) {
            discovered = true;
        }
    }

    public void setInsidePlayerTerritory(
            boolean insidePlayerTerritory
    ) {
        this.insidePlayerTerritory =
                insidePlayerTerritory;
    }

    public int getBaseMovementCost() {
        return terrainType.getBaseMovementCost();
    }

    public boolean isNormallyPassable() {
        return terrainType.isNormallyPassable();
    }

    public boolean isConstructionAllowed() {
        return terrainType.isConstructionAllowed();
    }

    private void copyResourceAmounts(
            Map<NaturalResourceType, Integer> source
    ) {
        for (Map.Entry<NaturalResourceType, Integer> entry :
                source.entrySet()) {

            NaturalResourceType resourceType =
                    Objects.requireNonNull(
                            entry.getKey(),
                            "Natural resource type cannot be null"
                    );

            Integer initialAmount =
                    Objects.requireNonNull(
                            entry.getValue(),
                            "Natural resource amount cannot be null"
                    );

            resourceDeposits.put(
                    resourceType,
                    new NaturalResourceDeposit(
                            resourceType,
                            initialAmount
                    )
            );
        }
    }

    private NaturalResourceDeposit getRequiredDeposit(
            NaturalResourceType resourceType
    ) {
        return getNaturalResourceDeposit(resourceType)
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Hex does not contain resource: " +
                                        resourceType
                        )
                );
    }

    private static Map<NaturalResourceType, Integer>
    createDefaultResourceAmounts(
            Set<NaturalResourceType> naturalResources
    ) {
        Objects.requireNonNull(
                naturalResources,
                "Natural resources cannot be null"
        );

        EnumMap<NaturalResourceType, Integer> amounts =
                new EnumMap<>(NaturalResourceType.class);

        for (NaturalResourceType resourceType :
                naturalResources) {

            amounts.put(
                    Objects.requireNonNull(
                            resourceType,
                            "Natural resource type cannot be null"
                    ),
                    DEFAULT_DEPOSIT_AMOUNT
            );
        }

        return amounts;
    }

    @Override
    public String toString() {
        return "HexTile{" +
                "coordinate=" + coordinate +
                ", terrainType=" + terrainType +
                ", resourceDeposits=" + resourceDeposits +
                ", discovered=" + discovered +
                ", visible=" + visible +
                ", insidePlayerTerritory=" +
                insidePlayerTerritory +
                '}';
    }
}
