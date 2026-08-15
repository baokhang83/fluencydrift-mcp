package io.github.baokhang83.fluencydrift;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpServerTransportProvider;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/** Registers FluencyDrift's fixed, read-only MCP tool contract. */
public final class McpServerFactory {
    private static final Map<String, Object> REPOSITORY_ROOT_SCHEMA = Map.of(
            "type", "object",
            "properties", Map.of("repository_root", Map.of(
                    "type", "string",
                    "description", "Absolute or relative root of the repository to inspect.")),
            "required", List.of("repository_root"),
            "additionalProperties", false);

    private final ObjectMapper objectMapper;
    private final ComparisonService comparisonService;

    public McpServerFactory(ObjectMapper objectMapper) {
        this(objectMapper, new ComparisonService(objectMapper));
    }

    McpServerFactory(ObjectMapper objectMapper, ComparisonService comparisonService) {
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper");
        this.comparisonService = Objects.requireNonNull(comparisonService, "comparisonService");
    }

    public McpSyncServer create(McpServerTransportProvider transportProvider) {
        return McpServer.sync(transportProvider)
                .serverInfo("fluencydrift-mcp", "0.1.0")
                .capabilities(McpSchema.ServerCapabilities.builder().tools(true).build())
                .toolCall(tool("get_store_snapshot", "Read the resolved FluencyLoop store snapshot."),
                        (exchange, request) -> result(request,
                                ignored -> comparisonService.getStoreSnapshot(repositoryRoot(request))))
                .toolCall(tool("get_repository_snapshot", "Read the root-confined repository snapshot."),
                        (exchange, request) -> result(request,
                                ignored -> comparisonService.getRepositorySnapshot(repositoryRoot(request))))
                .toolCall(tool("check_drift", "Compare FluencyLoop evidence with the repository."),
                        (exchange, request) -> result(request,
                                ignored -> comparisonService.checkDrift(repositoryRoot(request))))
                .build();
    }

    private McpSchema.Tool tool(String name, String description) {
        return McpSchema.Tool.builder(name, REPOSITORY_ROOT_SCHEMA).description(description).build();
    }

    private Path repositoryRoot(McpSchema.CallToolRequest request) {
        return Path.of((String) request.arguments().get("repository_root"));
    }

    private McpSchema.CallToolResult result(
            McpSchema.CallToolRequest request,
            Function<McpSchema.CallToolRequest, JsonNode> operation) {
        try {
            return textResult(objectMapper.writeValueAsString(operation.apply(request)), false);
        } catch (IllegalArgumentException | JsonProcessingException exception) {
            return textResult(objectMapper.valueToTree(comparisonService.error(exception.getMessage())).toString(), true);
        }
    }

    private McpSchema.CallToolResult textResult(String text, boolean error) {
        return McpSchema.CallToolResult.builder(List.of(new McpSchema.TextContent(text)))
                .isError(error)
                .build();
    }
}
