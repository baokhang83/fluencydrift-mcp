package io.github.baokhang83.fluencydrift.store;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;

/** Current resolved evidence, unrecognised evidence, and non-fatal read diagnostics. */
public record StoreSnapshot(
        List<ResolvedStoreRecord> resolvedRecords,
        List<JsonNode> unkeyedRecords,
        List<StoreReadError> readErrors) {

    public StoreSnapshot {
        resolvedRecords = List.copyOf(resolvedRecords);
        unkeyedRecords = List.copyOf(unkeyedRecords);
        readErrors = List.copyOf(readErrors);
    }
}
