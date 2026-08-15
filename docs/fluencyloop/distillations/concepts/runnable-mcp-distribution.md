# Runnable MCP distribution

The Maven build attaches a shaded `-all.jar` whose main class starts the [[mcp-stdio-boundary]].
It can be launched with `java -jar` without assembling a classpath.

An integration test starts that exact artifact as a child process and exchanges MCP messages over
stdio. This verifies the distributed protocol surface, including tool discovery and invocation,
where normal in-process unit tests cannot.
