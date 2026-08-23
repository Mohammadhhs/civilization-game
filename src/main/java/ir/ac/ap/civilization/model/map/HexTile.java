package ir.ac.ap.civilization.model.map;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

public final class HexTile {

    private final HexCoordinate coordinate;
    private final TerrainType terrainType;
    private final Set<NaturalResourceType> naturalResources;

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
                Collections.<NaturalResourceType>emptySet()
        );
    }

    public HexTile(
            HexCoordinate coordinate,
            TerrainType terrainType,
            Set<NaturalResourceType> naturalResources
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
                naturalResources,
                "Natural resources cannot be null"
        );

        EnumSet<NaturalResourceType> resourcesCopy =
                EnumSet.noneOf(NaturalResourceType.class);

        for (NaturalResourceType resource : naturalResources) {
            resourcesCopy.add(
                    Objects.requireNonNull(
                            resource,
                            "Natural resource cannot be null"
                    )
            );
        }

        this.naturalResources =
                Collections.unmodifiableSet(resourcesCopy);
    }

    public HexCoordinate getCoordinate() {
        return coordinate;
    }

    public TerrainType getTerrainType() {
        return terrainType;
    }

    public Set<NaturalResourceType> getNaturalResources() {
        return naturalResources;
    }

    public boolean hasNaturalResource(
            NaturalResourceType resource
    ) {
        Objects.requireNonNull(
                resource,
                "Natural resource cannot be null"
        );

        return naturalResources.contains(resource);
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
        this.insidePlayerTerritory = insidePlayerTerritory;
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

    @Override
    public String toString() {
        return "HexTile{" +
                "coordinate=" + coordinate +
                ", terrainType=" + terrainType +
                ", naturalResources=" + naturalResources +
                ", discovered=" + discovered +
                ", visible=" + visible +
                ", insidePlayerTerritory=" +
                insidePlayerTerritory +
                '}';
    }
}