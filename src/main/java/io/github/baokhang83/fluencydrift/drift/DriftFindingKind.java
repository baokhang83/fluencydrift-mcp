package io.github.baokhang83.fluencydrift.drift;

/** The way recorded source-path evidence differs from a repository snapshot. */
public enum DriftFindingKind {
    INVALID_EVIDENCE_PATH,
    MISSING_EVIDENCE,
    UNTRACKED_EVIDENCE
}
