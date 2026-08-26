package ir.ac.ap.civilization.model.game;

public enum SaveSlot {
    MANUAL_1("manual-1.sav"),
    MANUAL_2("manual-2.sav"),
    MANUAL_3("manual-3.sav"),
    AUTOSAVE("autosave.sav");

    private final String fileName;
    SaveSlot(String fileName) { this.fileName = fileName; }
    public String getFileName() { return fileName; }
}
