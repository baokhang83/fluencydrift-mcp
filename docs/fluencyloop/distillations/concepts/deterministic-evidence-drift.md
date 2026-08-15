# Deterministic evidence drift

The analyzer in [[src/main/java/io/github/baokhang83/fluencydrift/drift/EvidenceDriftAnalyzer.java]]
compares source-path evidence from resolved concept records with a repository snapshot. It rejects
paths that escape the root and identifies missing or untracked evidence.

Findings sort by path and type, so the same inputs produce the same report order. When repository
tracking is incomplete, the report retains diagnostics and does not claim that a file is untracked.
