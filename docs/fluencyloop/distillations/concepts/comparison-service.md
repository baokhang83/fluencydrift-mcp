# Comparison service

The [[src/main/java/io/github/baokhang83/fluencydrift/ComparisonService.java]] is the read-only
application service behind the MCP tools. It coordinates store parsing, repository observation, and
drift analysis without owning MCP transport or schema details.

Its three operations return JSON-ready store snapshots, repository snapshots, and drift reports.
Expected inspection failures become explicit error payloads so the protocol boundary can respond
without concealing operational diagnostics.
