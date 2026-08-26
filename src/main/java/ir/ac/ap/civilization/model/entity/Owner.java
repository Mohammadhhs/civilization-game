package ir.ac.ap.civilization.model.entity;

import java.util.Objects;
import java.io.Serializable;

public final class Owner implements Serializable {

    public static final Owner PLAYER =
            new Owner(OwnerType.PLAYER, "player");
    public static final Owner NEUTRAL =
            new Owner(OwnerType.NEUTRAL, "neutral");

    private final OwnerType type;
    private final String identifier;

    public Owner(OwnerType type, String identifier) {
        this.type = Objects.requireNonNull(
                type,
                "Owner type cannot be null"
        );
        this.identifier = Objects.requireNonNull(
                identifier,
                "Owner identifier cannot be null"
        ).trim();
        if (this.identifier.isEmpty()) {
            throw new IllegalArgumentException(
                    "Owner identifier cannot be blank"
            );
        }
    }

    public static Owner tribe(int tribeId) {
        return numbered(OwnerType.TRIBE, "tribe", tribeId);
    }

    public static Owner barbarian(int groupId) {
        return numbered(
                OwnerType.BARBARIAN,
                "barbarian",
                groupId
        );
    }

    public static Owner wildlife(int groupId) {
        return numbered(
                OwnerType.WILDLIFE,
                "wildlife",
                groupId
        );
    }

    private static Owner numbered(
            OwnerType type,
            String prefix,
            int id
    ) {
        if (id <= 0) {
            throw new IllegalArgumentException(
                    "Owner group id must be positive"
            );
        }
        return new Owner(type, prefix + "-" + id);
    }

    public OwnerType getType() {
        return type;
    }

    public String getIdentifier() {
        return identifier;
    }

    public boolean isPlayer() {
        return type == OwnerType.PLAYER;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof Owner)) {
            return false;
        }
        Owner other = (Owner) object;
        return type == other.type &&
                identifier.equals(other.identifier);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, identifier);
    }

    @Override
    public String toString() {
        return type.name() + ":" + identifier;
    }
}
