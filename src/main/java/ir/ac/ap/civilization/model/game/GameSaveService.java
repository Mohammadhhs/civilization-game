package ir.ac.ap.civilization.model.game;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class GameSaveService {
    public static final int SAVE_VERSION = 2;

    private final Path directory;

    public GameSaveService(Path directory) {
        this.directory = Objects.requireNonNull(directory, "Save directory cannot be null");
    }

    public SaveSummary save(GameState gameState, SaveSlot slot, String name)
            throws IOException {
        Objects.requireNonNull(gameState, "Game state cannot be null");
        Objects.requireNonNull(slot, "Save slot cannot be null");
        if (!gameState.isStableForPersistence()) {
            throw new IllegalStateException("Game can only be saved in a stable state");
        }
        Files.createDirectories(directory);
        Path target = pathFor(slot);
        Path temporary = Files.createTempFile(directory, slot.name(), ".tmp");
        gameState.beginOperation(GameOperation.SAVING);
        try {
            Envelope envelope = new Envelope(
                    name == null || name.trim().isEmpty() ? slot.name() : name.trim(),
                    System.currentTimeMillis(), gameState
            );
            write(temporary, envelope);
            readAndValidate(temporary);
            moveAtomically(temporary, target);
            return envelope.summary(slot);
        } finally {
            gameState.completeOperation();
            Files.deleteIfExists(temporary);
        }
    }

    public GameState load(SaveSlot slot) throws IOException {
        Envelope envelope = readAndValidate(pathFor(slot));
        GameState loaded = envelope.gameState;
        loaded.completeOperation();
        loaded.refreshVisibility();
        return loaded;
    }

    public List<SaveSummary> list() {
        List<SaveSummary> summaries = new ArrayList<>();
        for (SaveSlot slot : SaveSlot.values()) {
            try {
                summaries.add(readAndValidate(pathFor(slot)).summary(slot));
            } catch (IOException ignored) {
                // Missing and corrupt slots are omitted; UI can query isCorrupt.
            }
        }
        return Collections.unmodifiableList(summaries);
    }

    public boolean isCorrupt(SaveSlot slot) {
        Path path = pathFor(slot);
        if (!Files.exists(path)) return false;
        try { readAndValidate(path); return false; }
        catch (IOException exception) { return true; }
    }

    public void delete(SaveSlot slot) throws IOException {
        Files.deleteIfExists(pathFor(slot));
    }

    private Path pathFor(SaveSlot slot) { return directory.resolve(slot.getFileName()); }

    private void write(Path path, Envelope envelope) throws IOException {
        try (ObjectOutputStream output = new ObjectOutputStream(
                new BufferedOutputStream(Files.newOutputStream(path)))) {
            output.writeObject(envelope);
        }
    }

    private Envelope readAndValidate(Path path) throws IOException {
        try (ObjectInputStream input = new ObjectInputStream(
                new BufferedInputStream(Files.newInputStream(path)))) {
            Object value = input.readObject();
            if (!(value instanceof Envelope)) throw new IOException("Invalid save payload");
            Envelope envelope = (Envelope) value;
            if (envelope.version != SAVE_VERSION) {
                throw new IOException("Unsupported save version: " + envelope.version);
            }
            if (envelope.gameState == null || envelope.gameState.getGameMap() == null) {
                throw new IOException("Incomplete save data");
            }
            return envelope;
        } catch (ClassNotFoundException | RuntimeException exception) {
            throw new IOException("Corrupt save file", exception);
        }
    }

    private void moveAtomically(Path source, Path target) throws IOException {
        try {
            Files.move(source, target, StandardCopyOption.ATOMIC_MOVE,
                    StandardCopyOption.REPLACE_EXISTING);
        } catch (AtomicMoveNotSupportedException exception) {
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static final class Envelope implements Serializable {
        private static final long serialVersionUID = 2L;
        private final int version = SAVE_VERSION;
        private final String name;
        private final long savedAt;
        private final GameState gameState;
        private Envelope(String name, long savedAt, GameState gameState) {
            this.name = name; this.savedAt = savedAt; this.gameState = gameState;
        }
        private SaveSummary summary(SaveSlot slot) {
            return new SaveSummary(slot, name, gameState.getTurnNumber(),
                    gameState.getPhaseTwoState().getSeason(), savedAt,
                    gameState.getTownHall().getCurrentLevel().getLevelNumber());
        }
    }
}
