package com.skala.warehouse_helpdesk.controller;

import java.util.List;
import java.util.Map;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.skala.warehouse_helpdesk.config.WarehouseAiProperties;
import com.skala.warehouse_helpdesk.rag.ChunkSearchResult;

@RestController
@RequestMapping("/api/admin/chunks")
public class AdminChunkController {

    private final VectorStore vectorStore;
    private final WarehouseAiProperties properties;

    public AdminChunkController(VectorStore vectorStore, WarehouseAiProperties properties) {
        this.vectorStore = vectorStore;
        this.properties = properties;
    }

    @GetMapping
    public List<ChunkSearchResult> searchChunks(
            @RequestParam("q") String query,
            @RequestParam(required = false) Integer topK) {
        int safeTopK = safeTopK(topK);

        return vectorStore.similaritySearch(SearchRequest.builder()
                    .query(query)
                    .topK(safeTopK)
                    .similarityThreshold(properties.rag().similarityThreshold())
                    .build())
                .stream()
                .map(this::toResult)
                .toList();
    }

    private ChunkSearchResult toResult(Document document) {
        Map<String, Object> metadata = document.getMetadata();

        return new ChunkSearchResult(
                metadataValue(metadata, "source"),
                metadataValue(metadata, "version"),
                document.getScore(),
                preview(document.getText()));
    }

    private String metadataValue(Map<String, Object> metadata, String key) {
        Object value = metadata.get(key);
        return value == null ? "" : value.toString();
    }

    private String preview(String text) {
        if (text == null || text.length() <= properties.rag().previewLength()) {
            return text;
        }

        return text.substring(0, properties.rag().previewLength()) + "...";
    }

    private int safeTopK(Integer topK) {
        int requestedTopK = topK == null ? properties.rag().topK() : topK;
        return Math.max(1, Math.min(requestedTopK, properties.rag().maxTopK()));
    }
}
