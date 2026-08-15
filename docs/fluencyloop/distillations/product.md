# FluencyDrift MCP

FluencyDrift MCP is a local Java 21 MCP server that compares FluencyLoop evidence with a supplied
repository. Its standard-input/standard-output boundary exposes exactly three read-only tools:
`get_store_snapshot`, `get_repository_snapshot`, and `check_drift`. Each requires a
`repository_root` and is schema-validated by the [[mcp-stdio-boundary]].

The [[comparison-service]] reads each FluencyLoop JSONL record independently, observes regular
files below the supplied canonical repository root, and checks their Git tracking state. It combines
both inputs into [[deterministic-evidence-drift]] findings for missing, untracked, and invalid
source-path evidence. Damaged store data, unavailable Git tracking, and inaccessible repository
paths remain visible diagnostics rather than hidden failures.

The shipped `target/fluencydrift-mcp-0.1.0-SNAPSHOT-all.jar` runs this contract directly over
stdio. Protocol framing and validation follow [[§6]], leaving the application code to coordinate
product operations.
