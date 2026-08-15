package io.github.baokhang83.fluencydrift.drift;

import io.github.baokhang83.fluencydrift.repository.RepositoryReadError;
import java.util.List;

/** Sorted evidence drift findings and any limits on the repository observation. */
public record DriftReport(
        List<DriftFinding> findings,
        boolean incomplete,
        List<RepositoryReadError> diagnostics) {

    public DriftReport {
        findings = List.copyOf(findings);
        diagnostics = List.copyOf(diagnostics);
    }
}
