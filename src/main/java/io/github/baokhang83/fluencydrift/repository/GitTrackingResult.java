package io.github.baokhang83.fluencydrift.repository;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;

/** The tracked paths reported by Git, or diagnostics when Git could not report them. */
record GitTrackingResult(Set<Path> trackedPaths, List<RepositoryReadError> errors) {
    GitTrackingResult {
        trackedPaths = Set.copyOf(trackedPaths);
        errors = List.copyOf(errors);
    }

    boolean isComplete() {
        return errors.isEmpty();
    }
}
