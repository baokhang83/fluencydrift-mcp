package io.github.baokhang83.fluencydrift.store;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Optional;

/** Defines which append-only records represent successive versions of the same value. */
final class StoreRecordIdentity {
    Optional<String> identify(JsonNode record) {
        if (!record.isObject()) {
            return Optional.empty();
        }

        return switch (text(record, "type")) {
            case "concept" -> key("concept", text(record, "name"));
            case "principle" -> key("principle", text(record, "number"));
            case "feature" -> key("feature", text(record, "feature"));
            case "session" -> compositeKey("session", text(record, "feature"), text(record, "session"));
            case "relation" -> compositeKey("relation", text(record, "from"), text(record, "to"), text(record, "kind"));
            case "requirement" -> key("requirement", text(record, "gap"));
            default -> Optional.empty();
        };
    }

    private Optional<String> key(String type, String value) {
        return value.isBlank() ? Optional.empty() : Optional.of(type + ":" + value);
    }

    private Optional<String> compositeKey(String type, String... values) {
        for (String value : values) {
            if (value.isBlank()) {
                return Optional.empty();
            }
        }
        return Optional.of(type + ":" + String.join(":", values));
    }

    private String text(JsonNode record, String field) {
        JsonNode value = record.path(field);
        return value.isTextual() ? value.textValue() : "";
    }
}
