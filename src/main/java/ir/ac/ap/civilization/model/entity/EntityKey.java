package ir.ac.ap.civilization.model.entity;

import java.util.Objects;

public final class EntityKey {

    private final EntityKind kind;
    private final int id;

    public EntityKey(EntityKind kind, int id) {
        this.kind = Objects.requireNonNull(
                kind,
                "Entity kind cannot be null"
        );
        if (id <= 0) {
            throw new IllegalArgumentException(
                    "Entity id must be positive"
            );
        }
        this.id = id;
    }

    public EntityKind getKind() {
        return kind;
    }

    public int getId() {
        return id;
    }

    public String toExternalForm() {
        return kind.name() + ":" + id;
    }

    public static EntityKey fromExternalForm(String value) {
        Objects.requireNonNull(
                value,
                "Entity key value cannot be null"
        );

        int separator = value.indexOf(':');
        if (separator <= 0 || separator == value.length() - 1) {
            throw new IllegalArgumentException(
                    "Invalid entity key: " + value
            );
        }

        try {
            EntityKind kind = EntityKind.valueOf(
                    value.substring(0, separator)
            );
            int id = Integer.parseInt(
                    value.substring(separator + 1)
            );
            return new EntityKey(kind, id);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "Invalid entity key: " + value,
                    exception
            );
        }
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof EntityKey)) {
            return false;
        }
        EntityKey other = (EntityKey) object;
        return id == other.id && kind == other.kind;
    }

    @Override
    public int hashCode() {
        return Objects.hash(kind, id);
    }

    @Override
    public String toString() {
        return toExternalForm();
    }
}
