# FluencyDrift MCP

FluencyDrift MCP is a local Java 21 service that compares FluencyLoop evidence with a supplied
repository. It now has both comparison inputs: a resolved local store snapshot and a
[[root-confined-repository-snapshot]].

The service reads each FluencyLoop JSONL record independently. It then observes regular files below
the supplied canonical repository root and checks their Git tracking state. It combines both inputs
into [[deterministic-evidence-drift]] findings for missing, untracked, and invalid source-path
evidence. Damaged store data, unavailable Git tracking, and inaccessible repository paths remain
visible diagnostics rather than hidden failures.
