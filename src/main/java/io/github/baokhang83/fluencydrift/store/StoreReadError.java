package io.github.baokhang83.fluencydrift.store;

import java.nio.file.Path;
import java.util.Objects;

/** A non-fatal problem encountered while reading local store evidence. */
public record StoreReadError(Path source, int lineNumber, String message) {
    public StoreReadError {
        source = Objects.requireNonNull(source, "source");
        message = Objects.requireNonNull(message, "message");
    }
}
