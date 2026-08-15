package io.github.baokhang83.fluencydrift.repository;

import java.nio.file.Path;
import java.util.Objects;

/** A regular file observed below the requested repository root. */
public record RepositoryFile(Path relativePath, boolean tracked) {
    public RepositoryFile {
        relativePath = Objects.requireNonNull(relativePath, "relativePath");
        if (relativePath.isAbsolute() || relativePath.startsWith("..")) {
            throw new IllegalArgumentException("relativePath must stay below the repository root");
        }
    }
}
