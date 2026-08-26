package ir.ac.ap.civilization.model.phase2;

import ir.ac.ap.civilization.model.infrastructure.InfrastructureState;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.io.Serializable;

public final class PhaseTwoState implements Serializable {

    public static final int TURNS_PER_SEASON = 5;

    private Season season = Season.SPRING;
    private int turnsInSeason;
    private final HappinessState happiness = new HappinessState();
    private final InfrastructureState infrastructure = new InfrastructureState();
    private final List<String> notifications = new ArrayList<>();

    public Season getSeason() { return season; }
    public int getTurnsInSeason() { return turnsInSeason; }
    public HappinessState getHappiness() { return happiness; }
    public InfrastructureState getInfrastructure() { return infrastructure; }
    public List<String> getNotifications() {
        return Collections.unmodifiableList(new ArrayList<>(notifications));
    }

    public void notifyPlayer(String message) {
        if (message == null || message.trim().isEmpty()) return;
        notifications.add(message);
        while (notifications.size() > 30) notifications.remove(0);
    }

    public void advanceSeason() {
        turnsInSeason++;
        if (turnsInSeason >= TURNS_PER_SEASON) {
            turnsInSeason = 0;
            season = season.next();
            notifyPlayer("Season changed to " + season);
        }
    }
}
