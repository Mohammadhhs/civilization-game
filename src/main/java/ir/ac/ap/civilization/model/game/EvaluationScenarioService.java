package ir.ac.ap.civilization.model.game;

import ir.ac.ap.civilization.model.building.SpecialBuildingType;
import ir.ac.ap.civilization.model.disaster.DisasterType;
import ir.ac.ap.civilization.model.entity.Owner;
import ir.ac.ap.civilization.model.map.HexCoordinate;
import ir.ac.ap.civilization.model.map.HexTile;
import ir.ac.ap.civilization.model.map.TerrainType;
import ir.ac.ap.civilization.model.resource.ResourceType;
import ir.ac.ap.civilization.model.tribe.Tribe;
import ir.ac.ap.civilization.model.unit.MilitaryUnit;
import ir.ac.ap.civilization.model.unit.NavalUnit;
import ir.ac.ap.civilization.model.unit.UnitType;

import java.util.List;

public final class EvaluationScenarioService {
    private final GameState gameState;

    public EvaluationScenarioService(GameState gameState) {
        this.gameState = gameState;
    }

    public String addResources() {
        for (ResourceType type : ResourceType.values()) {
            gameState.getTownHall().getStockpile().add(type, 100);
        }
        return "Added 100 of every resource";
    }

    public String prepareCombat() {
        List<HexCoordinate> neighbors = gameState.getTownHallCoordinate().getNeighbors();
        HexCoordinate attacker = neighbors.get(0);
        HexCoordinate defender = attacker.getNeighbors().get(0);
        if (!gameState.getGameMap().containsCoordinate(defender)) {
            defender = neighbors.get(1);
        }
        gameState.addUnit(new MilitaryUnit(
                gameState.reserveUnitId(), UnitType.SWORDSMAN, attacker
        ));
        gameState.addUnit(new MilitaryUnit(
                gameState.reserveUnitId(), UnitType.ARCHER, attacker
        ));
        gameState.addUnit(new MilitaryUnit(
                gameState.reserveUnitId(), UnitType.SWORDSMAN, defender,
                Owner.barbarian(7), 100
        ));
        gameState.getGameMap().getTile(attacker).get().discover();
        gameState.getGameMap().getTile(defender).get().discover();
        return "Combat ready: attack " + defender + " from " + attacker;
    }

    public String prepareDiplomacy() {
        for (Tribe tribe : gameState.getTribes()) {
            tribe.discover();
            tribe.changeRelation(60 - tribe.getRelation());
        }
        return "All tribes discovered and made allied";
    }

    public String prepareInfrastructure() {
        List<HexCoordinate> neighbors = gameState.getTownHallCoordinate().getNeighbors();
        HexCoordinate first = neighbors.get(0);
        HexCoordinate second = neighbors.get(1);
        gameState.getPhaseTwoState().getInfrastructure().buildRoad(first);
        gameState.getPhaseTwoState().getInfrastructure().buildRoad(second);
        if (first.isAdjacentTo(second)) {
            gameState.getPhaseTwoState().getInfrastructure().addRiver(first, second);
            gameState.getPhaseTwoState().getInfrastructure().buildWall(first, second);
        }
        return "Road, river and wall demonstration prepared";
    }

    public String prepareNaval() {
        HexTile sea = null;
        HexTile coast = null;
        for (HexTile tile : gameState.getGameMap().getTiles()) {
            if (tile.getTerrainType() != TerrainType.SEA) continue;
            for (HexTile neighbor : gameState.getGameMap().getValidNeighbors(tile.getCoordinate())) {
                if (neighbor.getTerrainType() != TerrainType.SEA &&
                        neighbor.isConstructionAllowed() &&
                        !neighbor.getCoordinate().equals(
                                gameState.getTownHallCoordinate()) &&
                        !gameState.getResourceBuildingAt(neighbor.getCoordinate()).isPresent() &&
                        !gameState.getSettlementAt(neighbor.getCoordinate()).isPresent() &&
                        !gameState.getSpecialBuildingAt(neighbor.getCoordinate()).isPresent()) {
                    sea = tile; coast = neighbor; break;
                }
            }
            if (coast != null) break;
        }
        if (sea == null || coast == null) return "No coastal scenario is available";
        sea.discover(); coast.discover();
        gameState.addSpecialBuilding(
                SpecialBuildingType.DOCK, coast.getCoordinate(), Owner.PLAYER
        );
        gameState.addUnit(new NavalUnit(
                gameState.reserveUnitId(), UnitType.GALLEY,
                sea.getCoordinate()
        ));
        return "Dock and Galley prepared at " + coast.getCoordinate();
    }

    public String triggerDisaster(DisasterType type) {
        new DisasterService(gameState).apply(type, gameState.getTownHallCoordinate());
        return type + " triggered around Town Hall";
    }

    public String advanceSeason() {
        for (int index = 0; index < 5; index++) {
            gameState.getPhaseTwoState().advanceSeason();
        }
        return "Season changed to " + gameState.getPhaseTwoState().getSeason();
    }
}
