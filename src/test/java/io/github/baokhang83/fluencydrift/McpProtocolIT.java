package io.github.baokhang83.fluencydrift;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class McpProtocolIT {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @TempDir
    Path repositoryRoot;

    @Test
    void executableListsAndCallsOnlyTheThreeReadOnlyTools() throws Exception {
        Files.createDirectories(repositoryRoot.resolve("docs/fluencyloop/store"));
        Files.writeString(repositoryRoot.resolve("docs/fluencyloop/store/concepts.jsonl"), "");

        Process process = new ProcessBuilder(javaExecutable(), "-jar", System.getProperty("application.jar"))
                .redirectError(ProcessBuilder.Redirect.PIPE)
                .start();
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream(), StandardCharsets.UTF_8));
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
            send(writer, """
                    {"jsonrpc":"2.0","id":1,"method":"initialize","params":{"protocolVersion":"2025-11-25","capabilities":{},"clientInfo":{"name":"integration-test","version":"1"}}}
                    """);
            assertEquals("2.0", response(reader, 1).path("jsonrpc").asText());
            send(writer, """
                    {"jsonrpc":"2.0","method":"notifications/initialized","params":{}}
                    """);
            send(writer, """
                    {"jsonrpc":"2.0","id":2,"method":"tools/list","params":{}}
                    """);

            JsonNode listedTools = response(reader, 2).path("result").path("tools");
            assertEquals(Set.of("get_store_snapshot", "get_repository_snapshot", "check_drift"),
                    listedTools.valueStream().map(tool -> tool.path("name").asText()).collect(java.util.stream.Collectors.toSet()));
            assertTrue(listedTools.valueStream().allMatch(tool -> tool.path("inputSchema").path("required").toString().contains("repository_root")));

            send(writer, """
                    {"jsonrpc":"2.0","id":3,"method":"tools/call","params":{"name":"check_drift","arguments":{"repository_root":"%s"}}}
                    """.formatted(json(repositoryRoot.toString())));
            JsonNode callResult = response(reader, 3).path("result");
            assertFalse(callResult.path("isError").asBoolean());
            JsonNode payload = objectMapper.readTree(callResult.path("content").get(0).path("text").asText());
            assertTrue(payload.path("incomplete").asBoolean());
        } finally {
            process.destroyForcibly();
            process.waitFor();
        }
    }

    private void send(BufferedWriter writer, String message) throws IOException {
        writer.write(message.strip());
        writer.newLine();
        writer.flush();
    }

    private JsonNode response(BufferedReader reader, int id) throws IOException {
        String line;
        while ((line = reader.readLine()) != null) {
            JsonNode response = objectMapper.readTree(line);
            if (response.path("id").asInt() == id) {
                return response;
            }
        }
        throw new IOException("Server closed before responding to request " + id);
    }

    private String javaExecutable() {
        return Path.of(System.getProperty("java.home"), "bin", "java").toString();
    }

    private String json(String value) {
        try {
            return objectMapper.writeValueAsString(value).substring(1, objectMapper.writeValueAsString(value).length() - 1);
        } catch (IOException exception) {
            throw new IllegalStateException(exception);
        }
    }
}
