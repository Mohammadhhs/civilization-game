package ir.ac.ap.civilization.model.production;

import ir.ac.ap.civilization.model.building.TownHall;

public interface ProductionCommand {

    String getName();

    int getRemainingTurns();

    void advanceTurn();

    boolean isCompleted();

    void complete(TownHall townHall);
}