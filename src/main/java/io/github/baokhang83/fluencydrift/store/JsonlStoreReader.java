package io.github.baokhang83.fluencydrift.store;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/** Builds a deterministic current-state view from FluencyLoop's append-only JSONL files. */
public final class JsonlStoreReader {
    private final ObjectMapper objectMapper;
    private final StoreLayout storeLayout;
    private final StoreRecordIdentity identities;

    public JsonlStoreReader(ObjectMapper objectMapper) {
        this(objectMapper, new StoreLayout(), new StoreRecordIdentity());
    }

    JsonlStoreReader(ObjectMapper objectMapper, StoreLayout storeLayout, StoreRecordIdentity identities) {
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper");
        this.storeLayout = Objects.requireNonNull(storeLayout, "storeLayout");
        this.identities = Objects.requireNonNull(identities, "identities");
    }

    public StoreSnapshot read(Path repositoryRoot) {
        Map<String, ResolvedStoreRecord> resolved = new TreeMap<>();
        List<JsonNode> unkeyed = new ArrayList<>();
        List<StoreReadError> errors = new ArrayList<>();

        for (Path jsonlFile : storeFiles(repositoryRoot, errors)) {
            readFile(jsonlFile, resolved, unkeyed, errors);
        }
        return new StoreSnapshot(List.copyOf(resolved.values()), unkeyed, errors);
    }

    private List<Path> storeFiles(Path repositoryRoot, List<StoreReadError> errors) {
        Path storeDirectory = storeLayout.storeDirectory(repositoryRoot);
        try {
            return storeLayout.jsonlFiles(repositoryRoot);
        } catch (IOException exception) {
            errors.add(new StoreReadError(storeDirectory, 0, exception.getMessage()));
            return List.of();
        }
    }

    private void readFile(
            Path source,
            Map<String, ResolvedStoreRecord> resolved,
            List<JsonNode> unkeyed,
            List<StoreReadError> errors) {
        try (BufferedReader reader = Files.newBufferedReader(source)) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                readLine(source, lineNumber, line, resolved, unkeyed, errors);
            }
        } catch (IOException exception) {
            errors.add(new StoreReadError(source, 0, exception.getMessage()));
        }
    }

    private void readLine(
            Path source,
            int lineNumber,
            String line,
            Map<String, ResolvedStoreRecord> resolved,
            List<JsonNode> unkeyed,
            List<StoreReadError> errors) {
        try {
            JsonNode record = objectMapper.readTree(line);
            identities.identify(record)
                    .ifPresentOrElse(
                            identity -> resolved.put(identity, new ResolvedStoreRecord(identity, record, source, lineNumber)),
                            () -> unkeyed.add(record));
        } catch (IOException exception) {
            errors.add(new StoreReadError(source, lineNumber, exception.getMessage()));
        }
    }
}
