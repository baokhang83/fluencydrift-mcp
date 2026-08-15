package io.github.baokhang83.fluencydrift.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class RepositoryObserverTest {
    @TempDir
    Path repositoryRoot;

    @Test
    void observesOnlyRegularFilesBelowTheCanonicalRoot() throws IOException {
        write("src/Main.java");
        write(".git/config");
        Path externalFile = Files.createTempFile("fluencydrift-external", ".txt");
        Files.createSymbolicLink(repositoryRoot.resolve("external-link"), externalFile);

        RepositorySnapshot snapshot = new RepositoryObserver(root ->
                new GitTrackingResult(Set.of(Path.of("src/Main.java")), List.of())).observe(repositoryRoot);

        assertEquals(repositoryRoot.toRealPath(), snapshot.canonicalRoot());
        assertEquals(1, snapshot.files().size());
        assertEquals(Path.of("src/Main.java"), snapshot.files().getFirst().relativePath());
        assertTrue(snapshot.files().getFirst().tracked());
        assertTrue(snapshot.isComplete());
    }

    @Test
    void preservesFilesWhenGitTrackingIsUnavailable() throws IOException {
        write("notes.txt");

        RepositorySnapshot snapshot = new RepositoryObserver(root ->
                new GitTrackingResult(Set.of(), List.of(new RepositoryReadError(root, "git is unavailable"))))
                .observe(repositoryRoot);

        assertEquals(1, snapshot.files().size());
        assertFalse(snapshot.files().getFirst().tracked());
        assertFalse(snapshot.trackingComplete());
        assertFalse(snapshot.isComplete());
        assertEquals("git is unavailable", snapshot.readErrors().getFirst().message());
    }

    @Test
    void reportsAnInvalidRootWithoutWalkingIt() {
        Path missingRoot = repositoryRoot.resolve("missing");

        RepositorySnapshot snapshot = new RepositoryObserver(root -> {
            throw new AssertionError("Git must not run for an invalid root");
        }).observe(missingRoot);

        assertTrue(snapshot.files().isEmpty());
        assertFalse(snapshot.trackingComplete());
        assertEquals(1, snapshot.readErrors().size());
    }

    private void write(String relativePath) throws IOException {
        Path file = repositoryRoot.resolve(relativePath);
        Files.createDirectories(file.getParent());
        Files.writeString(file, "content");
    }
}
