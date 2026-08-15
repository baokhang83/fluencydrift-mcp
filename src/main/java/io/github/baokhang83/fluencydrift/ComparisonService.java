package io.github.baokhang83.fluencydrift;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.baokhang83.fluencydrift.drift.EvidenceDriftAnalyzer;
import io.github.baokhang83.fluencydrift.repository.RepositoryObserver;
import io.github.baokhang83.fluencydrift.repository.RepositorySnapshot;
import io.github.baokhang83.fluencydrift.store.JsonlStoreReader;
import io.github.baokhang83.fluencydrift.store.StoreSnapshot;
import java.nio.file.Path;
import java.util.Objects;

/** Coordinates read-only store, repository, and drift operations for MCP tool handlers. */
public final class ComparisonService {
    private final ObjectMapper objectMapper;
    private final JsonlStoreReader storeReader;
    private final RepositoryObserver repositoryObserver;
    private final EvidenceDriftAnalyzer driftAnalyzer;

    public ComparisonService(ObjectMapper objectMapper) {
        this(objectMapper, new JsonlStoreReader(objectMapper), new RepositoryObserver(), new EvidenceDriftAnalyzer());
    }

    ComparisonService(
            ObjectMapper objectMapper,
            JsonlStoreReader storeReader,
            RepositoryObserver repositoryObserver,
            EvidenceDriftAnalyzer driftAnalyzer) {
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper");
        this.storeReader = Objects.requireNonNull(storeReader, "storeReader");
        this.repositoryObserver = Objects.requireNonNull(repositoryObserver, "repositoryObserver");
        this.driftAnalyzer = Objects.requireNonNull(driftAnalyzer, "driftAnalyzer");
    }

    public JsonNode getStoreSnapshot(Path repositoryRoot) {
        return objectMapper.valueToTree(storeReader.read(repositoryRoot));
    }

    public JsonNode getRepositorySnapshot(Path repositoryRoot) {
        return objectMapper.valueToTree(repositoryObserver.observe(repositoryRoot));
    }

    public JsonNode checkDrift(Path repositoryRoot) {
        StoreSnapshot store = storeReader.read(repositoryRoot);
        RepositorySnapshot repository = repositoryObserver.observe(repositoryRoot);
        return objectMapper.valueToTree(driftAnalyzer.analyze(store, repository));
    }

    public JsonNode error(String message) {
        ObjectNode response = objectMapper.createObjectNode();
        response.put("error", message);
        return response;
    }
}
