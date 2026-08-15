package io.github.baokhang83.fluencydrift.repository;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** Uses the local Git executable to identify tracked files without adding a Git library. */
final class GitCliTrackingReader implements GitTrackingReader {
    @Override
    public GitTrackingResult readTrackedPaths(Path canonicalRoot) {
        ProcessBuilder command = new ProcessBuilder(
                "git", "-C", canonicalRoot.toString(), "ls-files", "--cached", "-z");
        command.redirectErrorStream(true);

        try {
            Process process = command.start();
            byte[] output = process.getInputStream().readAllBytes();
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                return failed(canonicalRoot, new String(output, StandardCharsets.UTF_8));
            }
            return new GitTrackingResult(parsePaths(output, canonicalRoot), List.of());
        } catch (IOException exception) {
            return failed(canonicalRoot, exception.getMessage());
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            return failed(canonicalRoot, "Git tracking was interrupted");
        }
    }

    private Set<Path> parsePaths(byte[] output, Path canonicalRoot) {
        String[] encodedPaths = new String(output, StandardCharsets.UTF_8).split("\\u0000");
        Set<Path> paths = new LinkedHashSet<>();
        for (String encodedPath : encodedPaths) {
            if (!encodedPath.isEmpty()) {
                paths.add(safeRelativePath(encodedPath, canonicalRoot));
            }
        }
        return paths;
    }

    private Path safeRelativePath(String encodedPath, Path canonicalRoot) {
        Path path = Path.of(encodedPath).normalize();
        if (path.isAbsolute() || path.startsWith("..")) {
            throw new IllegalArgumentException("Git returned a path outside " + canonicalRoot);
        }
        return path;
    }

    private GitTrackingResult failed(Path canonicalRoot, String detail) {
        String message = detail == null || detail.isBlank() ? "Git tracking is unavailable" : detail.strip();
        return new GitTrackingResult(Set.of(), List.of(new RepositoryReadError(canonicalRoot, message)));
    }
}
