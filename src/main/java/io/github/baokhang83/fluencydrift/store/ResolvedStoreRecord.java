package io.github.baokhang83.fluencydrift.store;

import com.fasterxml.jackson.databind.JsonNode;
import java.nio.file.Path;
import java.util.Objects;

/** The effective value for one identified append-only store record. */
public record ResolvedStoreRecord(String identity, JsonNode record, Path source, int lineNumber) {
    public ResolvedStoreRecord {
        identity = Objects.requireNonNull(identity, "identity");
        record = Objects.requireNonNull(record, "record");
        source = Objects.requireNonNull(source, "source");
    }
}
