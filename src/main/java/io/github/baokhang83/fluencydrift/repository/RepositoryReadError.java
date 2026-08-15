package io.github.baokhang83.fluencydrift.repository;

import java.nio.file.Path;
import java.util.Objects;

/** A non-fatal problem encountered while observing a repository. */
public record RepositoryReadError(Path source, String message) {
    public RepositoryReadError {
        source = Objects.requireNonNull(source, "source");
        message = Objects.requireNonNull(message, "message");
    }
}
