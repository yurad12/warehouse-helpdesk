package com.skala.warehouse_helpdesk.controller;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.ai.document.Document;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.skala.warehouse_helpdesk.config.WarehouseAiProperties;
import com.skala.warehouse_helpdesk.rag.ChunkSearchResult;
import com.skala.warehouse_helpdesk.rag.KnowledgeSearchService;

@RestController
@RequestMapping("/api/admin/chunks")
public class AdminChunkController {

    private final KnowledgeSearchService knowledgeSearchService;
    private final WarehouseAiProperties properties;

    public AdminChunkController(
            KnowledgeSearchService knowledgeSearchService,
            WarehouseAiProperties properties) {
        this.knowledgeSearchService = knowledgeSearchService;
        this.properties = properties;
    }

    @GetMapping
    public List<ChunkSearchResult> searchChunks(
            @RequestParam("q") String query,
            @RequestParam(defaultValue = "POLICY") String documentType,
            @RequestParam(required = false) Integer topK) {
        return search(query, documentType, topK)
                .stream()
                .map(this::toResult)
                .toList();
    }

    private List<Document> search(String query, String documentType, Integer topK) {
        return switch (documentType.toUpperCase(Locale.ROOT)) {
            case "POLICY" -> topK == null
                    ? knowledgeSearchService.searchPolicies(query)
                    : knowledgeSearchService.searchPolicies(query, safeTopK(topK));
            case "RESOLUTION_CASE" -> topK == null
                    ? knowledgeSearchService.searchResolutionCases(query, properties.testUser().warehouseId())
                    : knowledgeSearchService.searchResolutionCases(
                            query,
                            properties.testUser().warehouseId(),
                            safeTopK(topK));
            default -> throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "documentType은 POLICY 또는 RESOLUTION_CASE여야 합니다.");
        };
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

    private int safeTopK(int topK) {
        return Math.max(1, Math.min(topK, properties.rag().maxTopK()));
    }
}
