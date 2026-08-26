package ir.ac.ap.civilization.model.game;

import ir.ac.ap.civilization.model.building.MaintainedBuilding;
import ir.ac.ap.civilization.model.disaster.DisasterReport;
import ir.ac.ap.civilization.model.disaster.DisasterType;
import ir.ac.ap.civilization.model.map.HexCoordinate;
import ir.ac.ap.civilization.model.map.HexTile;
import ir.ac.ap.civilization.model.unit.Unit;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class DisasterService {
    private final GameState gameState;

    public DisasterService(GameState gameState) {
        this.gameState = Objects.requireNonNull(gameState, "Game state cannot be null");
    }

    public DisasterReport rollEndTurnDisaster() {
        if (gameState.getRandomSource().nextInt(100) >=
                gameState.getDifficulty().getDisasterChancePercent()) return null;
        DisasterType[] types = DisasterType.values();
        DisasterType type = types[gameState.getRandomSource().nextInt(types.length)];
        List<HexTile> tiles = new ArrayList<>(gameState.getGameMap().getTiles());
        HexCoordinate center = tiles.get(
                gameState.getRandomSource().nextInt(tiles.size())
        ).getCoordinate();
        return apply(type, center);
    }

    public DisasterReport apply(DisasterType type, HexCoordinate center) {
        Objects.requireNonNull(type, "Disaster type cannot be null");
        Objects.requireNonNull(center, "Disaster center cannot be null");
        boolean ownsOperation = gameState.isStableForPersistence();
        if (ownsOperation) {
            gameState.beginOperation(GameOperation.RESOLVING_EVENT);
        }
        try {
            int radius = type == DisasterType.EARTHQUAKE ? 2 : 1;
            int unitDamage = unitDamage(type);
            int structureDamage = structureDamage(type);
            int affected = 0;

            for (Unit unit : new ArrayList<>(gameState.getUnits())) {
                if (center.distanceTo(unit.getPosition()) <= radius) {
                    unit.takeDamage(unitDamage);
                    unit.applyActionPointPenalty(unit.getCurrentActionPoints());
                    affected++;
                    if (unit.isDestroyed()) gameState.removeUnit(unit);
                }
            }
            for (MaintainedBuilding building :
                    new ArrayList<>(gameState.getMaintainedBuildings())) {
                if (center.distanceTo(building.getPosition()) <= radius) {
                    building.takeDamage(structureDamage);
                    affected++;
                    if (building.isDestroyed()) gameState.removeMaintainedBuilding(building);
                }
            }
            if (center.distanceTo(gameState.getTownHallCoordinate()) <= radius) {
                int allowed = Math.min(
                        structureDamage,
                        Math.max(0, gameState.getTownHall().getCurrentHp() - 1)
                );
                gameState.getTownHall().takeDamage(allowed);
                affected++;
            }
            gameState.getPhaseTwoState().notifyPlayer(
                    type + " affected " + affected + " entities"
            );
            gameState.getPhaseTwoState().showVisualEvent(
                    center, type.name().replace('_', ' ')
            );
            if (ownsOperation) gameState.completeOperation();
            return new DisasterReport(type, center, affected);
        } catch (RuntimeException exception) {
            if (ownsOperation) gameState.failOperation();
            throw exception;
        }
    }

    private int unitDamage(DisasterType type) {
        switch (type) {
            case VOLCANIC_ERUPTION: return 60;
            case WILDFIRE: return 30;
            case TORNADO: return 30;
            case BEAR_ATTACK: return 35;
            default: return 20;
        }
    }

    private int structureDamage(DisasterType type) {
        switch (type) {
            case EARTHQUAKE: return 10;
            case VOLCANIC_ERUPTION: return 60;
            case FLOOD: return 30;
            case SEA_STORM: return 30;
            default: return 20;
        }
    }
}
