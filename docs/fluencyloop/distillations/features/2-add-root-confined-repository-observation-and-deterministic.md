# T2: repository evidence drift

Before T2, FluencyDrift MCP resolved local store evidence but could not compare it with a repository.
It now creates a [[root-confined-repository-snapshot]] and combines it with the
[[resolved-store-snapshot]] to produce [[deterministic-evidence-drift]].

The comparison identifies missing, untracked, and invalid source-path evidence in a stable order.
When Git tracking cannot be read, it makes that limit explicit instead of claiming untracked drift.
Repository inspection follows [[§5]] and remains below the supplied canonical root.
