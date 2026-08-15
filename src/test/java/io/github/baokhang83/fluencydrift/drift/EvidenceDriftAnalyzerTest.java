package io.github.baokhang83.fluencydrift.drift;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.baokhang83.fluencydrift.repository.RepositoryFile;
import io.github.baokhang83.fluencydrift.repository.RepositoryReadError;
import io.github.baokhang83.fluencydrift.repository.RepositorySnapshot;
import io.github.baokhang83.fluencydrift.store.ResolvedStoreRecord;
import io.github.baokhang83.fluencydrift.store.StoreSnapshot;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

class EvidenceDriftAnalyzerTest {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final EvidenceDriftAnalyzer analyzer = new EvidenceDriftAnalyzer();

    @Test
    void findsMissingUntrackedAndEscapingEvidenceInStableOrder() throws Exception {
        StoreSnapshot store = storeWith("""
                {"type":"concept","realized_by":"src/Present.java\\nsrc/Untracked.java\\nmissing.txt\\n../escape.txt"}
                """);
        RepositorySnapshot repository = new RepositorySnapshot(
                Path.of("/repository"),
                List.of(
                        new RepositoryFile(Path.of("src/Untracked.java"), false),
                        new RepositoryFile(Path.of("src/Present.java"), true)),
                List.of(),
                true);

        DriftReport report = analyzer.analyze(store, repository);

        assertEquals(List.of(
                DriftFindingKind.INVALID_EVIDENCE_PATH,
                DriftFindingKind.MISSING_EVIDENCE,
                DriftFindingKind.UNTRACKED_EVIDENCE),
                report.findings().stream().map(DriftFinding::kind).toList());
        assertEquals(List.of("../escape.txt", "missing.txt", "src/Untracked.java"),
                report.findings().stream().map(DriftFinding::evidencePath).toList());
        assertFalse(report.incomplete());
    }

    @Test
    void marksTheReportIncompleteInsteadOfClaimingUntrackedEvidenceWithoutGit() throws Exception {
        StoreSnapshot store = storeWith("""
                {"type":"concept","realized_by":"src/Present.java"}
                """);
        RepositorySnapshot repository = new RepositorySnapshot(
                Path.of("/repository"),
                List.of(new RepositoryFile(Path.of("src/Present.java"), false)),
                List.of(new RepositoryReadError(Path.of("/repository"), "git is unavailable")),
                false);

        DriftReport report = analyzer.analyze(store, repository);

        assertTrue(report.findings().isEmpty());
        assertTrue(report.incomplete());
        assertEquals(1, report.diagnostics().size());
    }

    private StoreSnapshot storeWith(String json) throws Exception {
        return new StoreSnapshot(
                List.of(new ResolvedStoreRecord("concept:example", objectMapper.readTree(json), Path.of("store.jsonl"), 1)),
                List.of(),
                List.of());
    }
}
