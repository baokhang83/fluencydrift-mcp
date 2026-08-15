# T1: resolved local store evidence

Before T1, FluencyDrift MCP had no Java runtime foundation or product read model. It now produces a
deterministic current view of FluencyLoop's append-only evidence through the
[[resolved-store-snapshot]] record.

The snapshot keeps effective known records, unfamiliar records, and read diagnostics together.
This means one malformed local JSONL line does not hide valid evidence from the comparison work
that follows. The Maven build compiles for Java 21 and enforces the [[§3]] unit-test coverage floor.
