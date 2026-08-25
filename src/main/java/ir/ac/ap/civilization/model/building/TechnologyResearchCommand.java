package ir.ac.ap.civilization.model.building;

import ir.ac.ap.civilization.model.production.ProductionCommand;
import ir.ac.ap.civilization.model.technology.TechnologyType;

import java.util.Objects;

public final class TechnologyResearchCommand
        implements ProductionCommand {

    private final TechnologyType technologyType;
    private int remainingTurns;

    public TechnologyResearchCommand(
            TechnologyType technologyType
    ) {
        this.technologyType = Objects.requireNonNull(
                technologyType,
                "Technology type cannot be null"
        );

        this.remainingTurns =
                technologyType.getResearchTurns();
    }

    public TechnologyType getTechnologyType() {
        return technologyType;
    }

    @Override
    public String getName() {
        return "Research " + technologyType.getDisplayName();
    }

    @Override
    public int getRemainingTurns() {
        return remainingTurns;
    }

    @Override
    public void advanceTurn() {
        if (remainingTurns > 0) {
            remainingTurns--;
        }
    }

    @Override
    public boolean isCompleted() {
        return remainingTurns == 0;
    }

    @Override
    public void complete(TownHall townHall) {
        Objects.requireNonNull(
                townHall,
                "Town Hall cannot be null"
        );

        townHall.applyTechnology(technologyType);
    }
}
