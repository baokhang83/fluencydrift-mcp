# T3: stdio MCP tools and executable distribution

Before T3, the store, repository, and drift logic could be called only from Java code. It now ships
as an executable [[runnable-mcp-distribution]] with exactly three read-only MCP tools:
`get_store_snapshot`, `get_repository_snapshot`, and `check_drift`.

The [[mcp-stdio-boundary]] validates every `repository_root` input and delegates product work to the
[[comparison-service]]. The shaded JAR is exercised through a real stdio protocol exchange, which
keeps the shipped executable contract verified rather than inferred from unit tests.
