package io.github.baokhang83.fluencydrift;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ComparisonServiceTest {
    private final ComparisonService service = new ComparisonService(new ObjectMapper());

    @TempDir
    Path repositoryRoot;

    @Test
    void returnsEachReadOnlyComparisonView() throws IOException {
        Path store = repositoryRoot.resolve("docs/fluencyloop/store/concepts.jsonl");
        Files.createDirectories(store.getParent());
        Files.writeString(store, """
                {"type":"concept","name":"example","realized_by":"missing.txt"}
                """);

        JsonNode storeSnapshot = service.getStoreSnapshot(repositoryRoot);
        JsonNode repositorySnapshot = service.getRepositorySnapshot(repositoryRoot);
        JsonNode driftReport = service.checkDrift(repositoryRoot);

        assertEquals(1, storeSnapshot.path("resolvedRecords").size());
        assertTrue(repositorySnapshot.path("files").size() >= 1);
        assertEquals("missing.txt", driftReport.path("findings").get(0).path("evidencePath").asText());
    }

    @Test
    void serializesHandlerErrorsAsAnObject() {
        assertEquals("invalid root", service.error("invalid root").path("error").asText());
    }
}
