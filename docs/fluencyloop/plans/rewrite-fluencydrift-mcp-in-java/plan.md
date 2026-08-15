# Plan: rewrite fluencydrift-mcp in Java

started: 2026-08-15

## Goal & scope

- **Goal:** Deliver a local Java 21 MCP server that compares FluencyLoop store evidence with a
  supplied repository.
- **In scope:** Three read-only stdio tools, resilient JSONL store reading, confined repository
  observation, deterministic drift findings, an executable Maven artifact, and automated tests.
- **Out of scope / non-goals:** HTTP transport, write operations, and a user interface.

## Open questions

None. The public tool contract, Java baseline, and non-goals are settled.

## Architecture

### Concepts

- **MCP stdio boundary** — exposes exactly three schema-validated, read-only tools. It converts
  MCP requests and results but contains no comparison rules.
- **Comparison service** — coordinates one repository-root request across the transport-neutral
  store, repository, and drift components.
- **Resolved store snapshot** — reads append-only FluencyLoop JSONL files and produces current
  records plus diagnostics without failing the whole request on malformed input.
- **Root-confined repository snapshot** — observes regular files below one canonical root and
  combines them with Git tracking state.
- **Deterministic evidence drift** — compares recorded source-path evidence with repository
  state and returns sorted findings with explicit incompleteness.
- **Runnable MCP distribution** — packages the server as a Maven executable and verifies the
  real stdio protocol boundary.

The implementation uses the synchronous MCP Java SDK API. Filesystem, JSONL, and Git operations
are blocking. A reactive implementation would add an adapter layer but no value for this local
process server. The SDK owns stdio framing and tool-schema validation, so the project does not
implement a custom JSON-RPC loop.

### Relationships and flows

| from | relationship | to | why it matters |
|------|--------------|----|----------------|
| MCP stdio boundary | invokes | Comparison service | Keeps protocol code outside comparison rules. |
| Comparison service | reads | Resolved store snapshot | Each tool can return effective store state. |
| Comparison service | observes | Root-confined repository snapshot | Repository inspection stays bounded to the supplied root. |
| Deterministic evidence drift | compares | Store and repository snapshots | Missing and untracked evidence becomes a stable report. |
| Runnable MCP distribution | starts | MCP stdio boundary | Users run the same artifact verified by the build. |

## Task breakdown

| id | task (feature intent) | size | depends on |
|----|-----------------------|------|------------|
| T1 | Establish the Java 21 Maven foundation and implement resilient resolved JSONL store snapshots | M | — |
| T2 | Add root-confined repository observation and deterministic evidence drift analysis | M | T1 |
| T3 | Expose the three stdio MCP tools and ship the executable with end-to-end protocol verification | M | T2 |

## Roadmap & critical path

- **Phase 1 — comparison foundation:** T1 establishes the Java build and reliable store state.
- **Phase 2 — repository comparison:** T2 adds bounded observation and makes drift reports
  available to the application layer.
- **Phase 3 — public delivery:** T3 exposes the fixed MCP contract and verifies the shipped JAR.
- **Critical path:** T1 → T2 → T3. T2 depends on the store model from T1, and T3 needs both
  comparison inputs before it can expose a complete client contract.

## Constitution check

- **§1 SOLID design:** The MCP adapter, application flow, and comparison components have distinct
  responsibilities and dependency directions.
- **§2 Clean Code:** Each planned feature keeps names, boundaries, and tests focused on one
  behavior.
- **§3 Unit-test coverage floor:** The Maven build will enforce at least 65% unit-test instruction
  coverage. The executable protocol test supplements, but does not replace, unit coverage.

## Tickets

- [Milestone: rewrite fluencydrift-mcp in Java](https://github.com/baokhang83/fluencydrift-mcp/milestone/1)
- T1: [#1](https://github.com/baokhang83/fluencydrift-mcp/issues/1)
- T2: [#2](https://github.com/baokhang83/fluencydrift-mcp/issues/2)
- T3: [#3](https://github.com/baokhang83/fluencydrift-mcp/issues/3)
