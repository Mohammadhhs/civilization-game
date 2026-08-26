package ir.ac.ap.civilization.model.production;

import java.io.Serializable;

import ir.ac.ap.civilization.model.building.TownHall;

public interface ProductionCommand extends Serializable {

    String getName();

    int getRemainingTurns();

    void advanceTurn();

    boolean isCompleted();

    void complete(TownHall townHall);
}
