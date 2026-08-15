package io.github.baokhang83.fluencydrift.repository;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/** Observes regular files below one canonical root and combines them with Git tracking data. */
public final class RepositoryObserver {
    private final GitTrackingReader gitTrackingReader;

    public RepositoryObserver() {
        this(new GitCliTrackingReader());
    }

    RepositoryObserver(GitTrackingReader gitTrackingReader) {
        this.gitTrackingReader = Objects.requireNonNull(gitTrackingReader, "gitTrackingReader");
    }

    public RepositorySnapshot observe(Path repositoryRoot) {
        Path requestedRoot = repositoryRoot.toAbsolutePath().normalize();
        List<RepositoryReadError> errors = new ArrayList<>();
        Path canonicalRoot = canonicalRoot(requestedRoot, errors);
        if (!errors.isEmpty()) {
            return new RepositorySnapshot(canonicalRoot, List.of(), errors, false);
        }

        List<Path> observedPaths = observedPaths(canonicalRoot, errors);
        GitTrackingResult tracking = gitTrackingReader.readTrackedPaths(canonicalRoot);
        errors.addAll(tracking.errors());
        Set<Path> trackedPaths = new HashSet<>(tracking.trackedPaths());

        List<RepositoryFile> files = observedPaths.stream()
                .map(path -> new RepositoryFile(path, trackedPaths.contains(path)))
                .sorted(Comparator.comparing(file -> file.relativePath().toString()))
                .toList();
        return new RepositorySnapshot(canonicalRoot, files, errors, tracking.isComplete());
    }

    private Path canonicalRoot(Path requestedRoot, List<RepositoryReadError> errors) {
        try {
            if (!Files.isDirectory(requestedRoot, LinkOption.NOFOLLOW_LINKS)) {
                throw new IOException("Repository root is not a directory");
            }
            return requestedRoot.toRealPath();
        } catch (IOException exception) {
            errors.add(new RepositoryReadError(requestedRoot, exception.getMessage()));
            return requestedRoot;
        }
    }

    private List<Path> observedPaths(Path canonicalRoot, List<RepositoryReadError> errors) {
        List<Path> paths = new ArrayList<>();
        try {
            Files.walkFileTree(canonicalRoot, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult preVisitDirectory(Path directory, BasicFileAttributes attributes) {
                    return directory.getFileName().toString().equals(".git")
                            ? FileVisitResult.SKIP_SUBTREE
                            : FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) {
                    if (Files.isRegularFile(file, LinkOption.NOFOLLOW_LINKS)) {
                        paths.add(canonicalRoot.relativize(file));
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exception) {
                    errors.add(new RepositoryReadError(file, exception.getMessage()));
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException exception) {
            errors.add(new RepositoryReadError(canonicalRoot, exception.getMessage()));
        }
        return paths;
    }
}
