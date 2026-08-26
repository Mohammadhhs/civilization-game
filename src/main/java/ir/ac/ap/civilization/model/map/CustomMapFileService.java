package ir.ac.ap.civilization.model.map;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

public final class CustomMapFileService {

    public void save(CustomMapDesign design, Path target) throws IOException {
        Objects.requireNonNull(design, "Map design cannot be null");
        Objects.requireNonNull(target, "Target cannot be null");
        Path absolute = target.toAbsolutePath();
        Path parent = absolute.getParent();
        if (parent != null) Files.createDirectories(parent);
        Path temporary = Files.createTempFile(parent, "civilization-map-", ".tmp");
        try {
            try (ObjectOutputStream output = new ObjectOutputStream(
                    new BufferedOutputStream(Files.newOutputStream(temporary)))) {
                output.writeObject(design);
            }
            try {
                Files.move(temporary, absolute,
                        StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE);
            } catch (IOException notAtomic) {
                Files.move(temporary, absolute, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    public CustomMapDesign load(Path source) throws IOException {
        Objects.requireNonNull(source, "Source cannot be null");
        try (ObjectInputStream input = new ObjectInputStream(
                new BufferedInputStream(Files.newInputStream(source)))) {
            Object value = input.readObject();
            if (!(value instanceof CustomMapDesign)) {
                throw new IOException("The selected file is not a Civilization map");
            }
            CustomMapDesign design = (CustomMapDesign) value;
            design.toGameMap();
            return design;
        } catch (ClassNotFoundException exception) {
            throw new IOException("The custom map format is not supported", exception);
        }
    }
}
