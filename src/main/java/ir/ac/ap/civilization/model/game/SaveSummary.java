package ir.ac.ap.civilization.model.game;

import ir.ac.ap.civilization.model.phase2.Season;

public final class SaveSummary {
    private final SaveSlot slot;
    private final String name;
    private final int turn;
    private final Season season;
    private final long savedAtEpochMillis;
    private final int townHallLevel;

    SaveSummary(SaveSlot slot, String name, int turn, Season season,
                long savedAtEpochMillis, int townHallLevel) {
        this.slot = slot; this.name = name; this.turn = turn;
        this.season = season; this.savedAtEpochMillis = savedAtEpochMillis;
        this.townHallLevel = townHallLevel;
    }
    public SaveSlot getSlot() { return slot; }
    public String getName() { return name; }
    public int getTurn() { return turn; }
    public Season getSeason() { return season; }
    public long getSavedAtEpochMillis() { return savedAtEpochMillis; }
    public int getTownHallLevel() { return townHallLevel; }
}
