# FluencyDrift MCP

FluencyDrift MCP is a local Java 21 service that will compare FluencyLoop evidence with a supplied
repository. Its first working product flow resolves local evidence before later features observe
the repository and report drift.

The service starts with a repository root. It finds sorted FluencyLoop JSONL files, reads their
records independently, and returns a resolved store snapshot. The snapshot contains current known
records, unfamiliar records, and diagnostics for damaged input. This applies [[§4]] so one malformed
line does not hide other usable evidence.
