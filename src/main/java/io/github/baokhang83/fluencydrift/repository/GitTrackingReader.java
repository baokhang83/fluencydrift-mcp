package io.github.baokhang83.fluencydrift.repository;

import java.nio.file.Path;

/** Reads tracked paths for one canonical repository root. */
interface GitTrackingReader {
    GitTrackingResult readTrackedPaths(Path canonicalRoot);
}
