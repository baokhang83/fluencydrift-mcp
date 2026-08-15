package io.github.baokhang83.fluencydrift.repository;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class GitCliTrackingReaderTest {
    @TempDir
    Path repositoryRoot;

    @Test
    void returnsCachedGitPathsAsRelativePaths() throws Exception {
        Files.createDirectories(repositoryRoot.resolve("src"));
        Files.writeString(repositoryRoot.resolve("src/Tracked.java"), "class Tracked {}");
        runGit("init");
        runGit("add", "src/Tracked.java");

        GitTrackingResult result = new GitCliTrackingReader().readTrackedPaths(repositoryRoot.toRealPath());

        assertTrue(result.isComplete());
        assertTrue(result.trackedPaths().contains(Path.of("src/Tracked.java")));
    }

    @Test
    void reportsDiagnosticsWhenTheRootIsNotAGitRepository() throws IOException {
        GitTrackingResult result = new GitCliTrackingReader().readTrackedPaths(repositoryRoot.toRealPath());

        assertFalse(result.isComplete());
        assertTrue(result.trackedPaths().isEmpty());
        assertFalse(result.errors().isEmpty());
    }

    private void runGit(String... arguments) throws Exception {
        String[] command = new String[arguments.length + 3];
        command[0] = "git";
        command[1] = "-C";
        command[2] = repositoryRoot.toString();
        System.arraycopy(arguments, 0, command, 3, arguments.length);
        Process process = new ProcessBuilder(command).redirectErrorStream(true).start();
        process.getInputStream().readAllBytes();
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new IOException("Git command failed with exit code " + exitCode);
        }
    }
}
