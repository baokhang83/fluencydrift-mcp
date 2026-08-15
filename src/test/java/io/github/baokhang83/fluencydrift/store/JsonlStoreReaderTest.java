package io.github.baokhang83.fluencydrift.store;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class JsonlStoreReaderTest {
    private final JsonlStoreReader reader = new JsonlStoreReader(new ObjectMapper());

    @TempDir
    Path repositoryRoot;

    @Test
    void resolvesTheLatestRecordForEachKnownIdentity() throws IOException {
        writeStore("first.jsonl", """
                {"type":"concept","name":"snapshot","summary":"older"}
                {"type":"principle","number":"§1","rule":"focused"}
                """);
        writeStore("nested/second.jsonl", """
                {"type":"concept","name":"snapshot","summary":"newer"}
                {"type":"feature","feature":"T1"}
                """);

        StoreSnapshot snapshot = reader.read(repositoryRoot);

        assertEquals(3, snapshot.resolvedRecords().size());
        assertEquals("concept:snapshot", snapshot.resolvedRecords().getFirst().identity());
        assertEquals("newer", snapshot.resolvedRecords().getFirst().record().path("summary").asText());
        assertEquals("feature:T1", snapshot.resolvedRecords().get(1).identity());
        assertEquals("principle:§1", snapshot.resolvedRecords().get(2).identity());
    }

    @Test
    void keepsReadingAfterAMalformedLineAndReportsItsLocation() throws IOException {
        Path source = writeStore("events.jsonl", """
                {"type":"feature","feature":"T1"}
                this is not JSON
                {"type":"requirement","gap":"runtime"}
                """);

        StoreSnapshot snapshot = reader.read(repositoryRoot);

        assertEquals(2, snapshot.resolvedRecords().size());
        assertEquals(1, snapshot.readErrors().size());
        StoreReadError error = snapshot.readErrors().getFirst();
        assertEquals(source, error.source());
        assertEquals(2, error.lineNumber());
        assertTrue(error.message().contains("Unrecognized token"));
    }

    @Test
    void retainsUnknownRecordsWithoutPretendingTheyAreResolved() throws IOException {
        writeStore("future.jsonl", """
                {"type":"future-evidence","id":"later"}
                ["a future shape"]
                """);

        StoreSnapshot snapshot = reader.read(repositoryRoot);

        assertTrue(snapshot.resolvedRecords().isEmpty());
        assertEquals(2, snapshot.unkeyedRecords().size());
        assertTrue(snapshot.readErrors().isEmpty());
    }

    @Test
    void returnsAnEmptySnapshotWhenTheStoreDirectoryDoesNotExist() {
        StoreSnapshot snapshot = reader.read(repositoryRoot);

        assertTrue(snapshot.resolvedRecords().isEmpty());
        assertTrue(snapshot.unkeyedRecords().isEmpty());
        assertTrue(snapshot.readErrors().isEmpty());
    }

    private Path writeStore(String relativePath, String content) throws IOException {
        Path destination = repositoryRoot.resolve("docs/fluencyloop/store").resolve(relativePath);
        Files.createDirectories(destination.getParent());
        Files.writeString(destination, content);
        return destination;
    }
}
