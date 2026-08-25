package ir.ac.ap.civilization.model.game;

import ir.ac.ap.civilization.model.resource.ResourceType;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

public final class ResourceFlowForecast {

    private final Map<ResourceType, Integer> netChanges;

    ResourceFlowForecast(
            Map<ResourceType, Integer> netChanges
    ) {
        Objects.requireNonNull(
                netChanges,
                "Net changes cannot be null"
        );

        this.netChanges = Collections.unmodifiableMap(
                new EnumMap<>(netChanges)
        );
    }

    public int getNetChange(ResourceType resourceType) {
        Objects.requireNonNull(
                resourceType,
                "Resource type cannot be null"
        );

        Integer value = netChanges.get(resourceType);
        return value == null ? 0 : value;
    }

    public Map<ResourceType, Integer> asMap() {
        return netChanges;
    }
}
