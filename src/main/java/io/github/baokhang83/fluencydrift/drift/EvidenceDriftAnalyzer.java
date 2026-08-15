package io.github.baokhang83.fluencydrift.drift;

import com.fasterxml.jackson.databind.JsonNode;
import io.github.baokhang83.fluencydrift.repository.RepositoryFile;
import io.github.baokhang83.fluencydrift.repository.RepositorySnapshot;
import io.github.baokhang83.fluencydrift.store.ResolvedStoreRecord;
import io.github.baokhang83.fluencydrift.store.StoreSnapshot;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

/** Compares resolved concept source-path evidence with one repository snapshot. */
public final class EvidenceDriftAnalyzer {
    public DriftReport analyze(StoreSnapshot storeSnapshot, RepositorySnapshot repositorySnapshot) {
        Map<String, RepositoryFile> repositoryFiles = filesByPath(repositorySnapshot.files());
        List<DriftFinding> findings = new ArrayList<>();

        for (String evidencePath : evidencePaths(storeSnapshot.resolvedRecords())) {
            evaluate(evidencePath, repositoryFiles, repositorySnapshot.trackingComplete(), findings);
        }
        findings.sort(Comparator.comparing(DriftFinding::evidencePath).thenComparing(DriftFinding::kind));
        return new DriftReport(findings, !repositorySnapshot.isComplete(), repositorySnapshot.readErrors());
    }

    private Map<String, RepositoryFile> filesByPath(List<RepositoryFile> files) {
        Map<String, RepositoryFile> filesByPath = new HashMap<>();
        for (RepositoryFile file : files) {
            filesByPath.put(portablePath(file.relativePath()), file);
        }
        return filesByPath;
    }

    private TreeSet<String> evidencePaths(List<ResolvedStoreRecord> records) {
        TreeSet<String> paths = new TreeSet<>();
        for (ResolvedStoreRecord resolvedRecord : records) {
            JsonNode record = resolvedRecord.record();
            if ("concept".equals(record.path("type").asText()) && record.path("realized_by").isTextual()) {
                for (String path : record.path("realized_by").asText().split("\\R")) {
                    if (!path.isBlank()) {
                        paths.add(path);
                    }
                }
            }
        }
        return paths;
    }

    private void evaluate(
            String evidencePath,
            Map<String, RepositoryFile> repositoryFiles,
            boolean trackingComplete,
            List<DriftFinding> findings) {
        Path normalizedPath;
        try {
            normalizedPath = Path.of(evidencePath).normalize();
        } catch (InvalidPathException exception) {
            findings.add(new DriftFinding(DriftFindingKind.INVALID_EVIDENCE_PATH, evidencePath, "The path is invalid"));
            return;
        }
        if (normalizedPath.isAbsolute() || normalizedPath.startsWith("..")) {
            findings.add(new DriftFinding(
                    DriftFindingKind.INVALID_EVIDENCE_PATH,
                    evidencePath,
                    "The path escapes the repository root"));
            return;
        }

        RepositoryFile file = repositoryFiles.get(portablePath(normalizedPath));
        if (file == null) {
            findings.add(new DriftFinding(DriftFindingKind.MISSING_EVIDENCE, evidencePath, "The file is not present"));
        } else if (trackingComplete && !file.tracked()) {
            findings.add(new DriftFinding(DriftFindingKind.UNTRACKED_EVIDENCE, evidencePath, "The file is not tracked by Git"));
        }
    }

    private String portablePath(Path path) {
        return path.toString().replace(path.getFileSystem().getSeparator(), "/");
    }
}
