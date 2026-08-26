package ir.ac.ap.civilization.model.phase2;

public enum Season {
    SPRING, SUMMER, AUTUMN, WINTER;

    public Season next() {
        Season[] seasons = values();
        return seasons[(ordinal() + 1) % seasons.length];
    }
}
