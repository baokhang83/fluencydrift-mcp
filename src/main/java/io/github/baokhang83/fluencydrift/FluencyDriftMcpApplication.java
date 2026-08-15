package io.github.baokhang83.fluencydrift;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.json.McpJsonDefaults;
import io.modelcontextprotocol.server.transport.StdioServerTransportProvider;

/** Starts the FluencyDrift MCP server on the process standard streams. */
public final class FluencyDriftMcpApplication {
    private FluencyDriftMcpApplication() {
    }

    public static void main(String[] arguments) throws InterruptedException {
        var transport = new StdioServerTransportProvider(McpJsonDefaults.getMapper());
        new McpServerFactory(new ObjectMapper()).create(transport);
        Thread.currentThread().join();
    }
}
