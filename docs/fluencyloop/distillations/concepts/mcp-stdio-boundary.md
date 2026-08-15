# MCP stdio boundary

The [[src/main/java/io/github/baokhang83/fluencydrift/McpServerFactory.java]] uses the official MCP
Java SDK to expose the fixed read-only tool contract over standard input and output. It declares the
`repository_root` schema once per tool and lets the SDK handle JSON-RPC framing and validation.

The boundary converts the [[comparison-service]] result into text content and keeps transport
concerns separate from the comparison domain. This follows [[§6]].
