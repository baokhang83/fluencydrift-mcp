package io.github.baokhang83.fluencydrift.store;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;

/** Locates FluencyLoop JSONL store files below a repository root. */
final class StoreLayout {
    private static final Path STORE_DIRECTORY = Path.of("docs", "fluencyloop", "store");

    Path storeDirectory(Path repositoryRoot) {
        return repositoryRoot.resolve(STORE_DIRECTORY);
    }

    List<Path> jsonlFiles(Path repositoryRoot) throws IOException {
        Path storeDirectory = storeDirectory(repositoryRoot);
        if (!Files.isDirectory(storeDirectory)) {
            return List.of();
        }

        try (var paths = Files.walk(storeDirectory)) {
            return paths
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(".jsonl"))
                    .sorted(Comparator.comparing(path -> storeDirectory.relativize(path).toString()))
                    .toList();
        }
    }
}
