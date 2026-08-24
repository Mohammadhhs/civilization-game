package ir.ac.ap.civilization.model.technology;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

public final class TechnologyManager {

    public static final double NORMAL_MINING_MULTIPLIER = 1.0;
    public static final double PROFESSIONAL_TOOLS_MULTIPLIER = 1.5;

    private final EnumSet<TechnologyType> completedTechnologies;

    public TechnologyManager() {
        completedTechnologies =
                EnumSet.noneOf(TechnologyType.class);
    }

    public boolean isCompleted(TechnologyType technologyType) {
        Objects.requireNonNull(
                technologyType,
                "Technology type cannot be null"
        );

        return completedTechnologies.contains(technologyType);
    }

    public boolean arePrerequisitesCompleted(
            TechnologyType technologyType
    ) {
        Objects.requireNonNull(
                technologyType,
                "Technology type cannot be null"
        );

        return completedTechnologies.containsAll(
                technologyType.getPrerequisites()
        );
    }

    public boolean canResearch(TechnologyType technologyType) {
        return !isCompleted(technologyType)
                && arePrerequisitesCompleted(technologyType);
    }

    public void completeResearch(
            TechnologyType technologyType
    ) {
        if (isCompleted(technologyType)) {
            throw new IllegalStateException(
                    "Technology has already been researched"
            );
        }

        if (!arePrerequisitesCompleted(technologyType)) {
            throw new IllegalStateException(
                    "Technology prerequisites are not completed"
            );
        }

        completedTechnologies.add(technologyType);
    }

    public Set<TechnologyType> getCompletedTechnologies() {
        return Collections.unmodifiableSet(
                EnumSet.copyOf(completedTechnologies)
        );
    }

    public double getMiningProductionMultiplier() {
        if (isCompleted(TechnologyType.PROFESSIONAL_TOOLS)) {
            return PROFESSIONAL_TOOLS_MULTIPLIER;
        }

        return NORMAL_MINING_MULTIPLIER;
    }
}