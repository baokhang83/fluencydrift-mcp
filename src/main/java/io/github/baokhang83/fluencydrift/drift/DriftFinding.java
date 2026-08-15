package io.github.baokhang83.fluencydrift.drift;

import java.util.Objects;

/** One deterministic drift finding for recorded source-path evidence. */
public record DriftFinding(DriftFindingKind kind, String evidencePath, String detail) {
    public DriftFinding {
        kind = Objects.requireNonNull(kind, "kind");
        evidencePath = Objects.requireNonNull(evidencePath, "evidencePath");
        detail = Objects.requireNonNull(detail, "detail");
    }
}
