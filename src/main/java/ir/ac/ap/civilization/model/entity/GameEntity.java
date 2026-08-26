package ir.ac.ap.civilization.model.entity;

import java.io.Serializable;

public interface GameEntity extends Serializable {

    int getId();

    EntityKey getEntityKey();
}
