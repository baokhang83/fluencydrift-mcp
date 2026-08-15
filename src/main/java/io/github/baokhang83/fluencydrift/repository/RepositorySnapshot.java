package io.github.baokhang83.fluencydrift.repository;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

/** A root-confined file view and its Git tracking reliability. */
public record RepositorySnapshot(
        Path canonicalRoot,
        List<RepositoryFile> files,
        List<RepositoryReadError> readErrors,
        boolean trackingComplete) {

    public RepositorySnapshot {
        canonicalRoot = Objects.requireNonNull(canonicalRoot, "canonicalRoot");
        files = List.copyOf(files);
        readErrors = List.copyOf(readErrors);
    }

    public boolean isComplete() {
        return trackingComplete && readErrors.isEmpty();
    }
}
