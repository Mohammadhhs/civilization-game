package ir.ac.ap.civilization.model.map;

public enum TerrainType {

    FOREST(2, true, true),
    PLAIN(1, true, true),
    MOUNTAIN(3, true, true),
    GRASSLAND(1, true, true),

    SEA(2, false, false),
    MOUNTAIN_RANGE(Integer.MAX_VALUE, false, false);

    private final int baseMovementCost;
    private final boolean normallyPassable;
    private final boolean constructionAllowed;

    TerrainType(
            int baseMovementCost,
            boolean normallyPassable,
            boolean constructionAllowed
    ) {
        if (baseMovementCost <= 0) {
            throw new IllegalArgumentException(
                    "Movement cost must be positive"
            );
        }

        this.baseMovementCost = baseMovementCost;
        this.normallyPassable = normallyPassable;
        this.constructionAllowed = constructionAllowed;
    }

    public int getBaseMovementCost() {
        return baseMovementCost;
    }

    public boolean isNormallyPassable() {
        return normallyPassable;
    }

    public boolean isConstructionAllowed() {
        return constructionAllowed;
    }
}