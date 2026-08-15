# Constitution

**Project:** FluencyDrift MCP

## Constitution areas

### Guardrails

_No stance recorded yet._

### Architecture principles

Use SOLID principles and Clean Code principles.

### Test methodology

Maintain unit-test code coverage of at least 65%.

### Data and state

_No stance recorded yet._

### Dependencies

_No stance recorded yet._

### Security and privacy

_No stance recorded yet._

## Principles

### §1 — SOLID design

**Rule:** Use SOLID principles in production code design.

**Why:** Keeps responsibilities focused and dependencies understandable, so changes remain
localized as the codebase evolves.

### §2 — Clean Code

**Rule:** Apply Clean Code principles in production and test code.

**Why:** Keeps code readable and intention-revealing, preventing avoidable maintenance mistakes.

### §3 — Unit-test coverage floor

**Rule:** Maintain unit-test code coverage of at least 65%.

**Why:** Prevents the core behaviour from becoming unverified as the server evolves.

### §4 — Resilient local inspection

**Rule:** Preserve valid local evidence and report damaged input as diagnostics; do not fail the
whole inspection because one local record is malformed.

**Why:** Prevents one damaged JSONL line from hiding the remaining repository evidence.

### §5 — Root-confined repository inspection

**Rule:** Observe only regular, non-symbolic-link files below the supplied canonical repository
root, and exclude Git metadata.

**Why:** Prevents repository inspection from following a path outside the requested boundary.

### §6 — SDK-owned protocol handling

**Rule:** Use the official MCP SDK for protocol framing, transport behavior, and tool-schema
validation; keep application code focused on product operations.

**Why:** Prevents local server code from duplicating or drifting from the MCP protocol.
