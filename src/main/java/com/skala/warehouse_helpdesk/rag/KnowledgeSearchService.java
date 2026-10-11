package com.skala.warehouse_helpdesk.rag;

import java.util.List;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.stereotype.Service;

import com.skala.warehouse_helpdesk.config.WarehouseAiProperties;

@Service
public class KnowledgeSearchService {

    private final VectorStore vectorStore;
    private final WarehouseAiProperties properties;

    public KnowledgeSearchService(VectorStore vectorStore, WarehouseAiProperties properties) {
        this.vectorStore = vectorStore;
        this.properties = properties;
    }

    public List<Document> searchPolicies(String query) {
        return searchPolicies(query, properties.rag().topK());
    }

    public List<Document> searchPolicies(String query, int topK) {
        FilterExpressionBuilder filters = new FilterExpressionBuilder();
        return search(query, topK, filters.eq("documentType", "POLICY").build());
    }

    public List<Document> searchResolutionCases(String query, String warehouseId) {
        return searchResolutionCases(query, warehouseId, properties.rag().caseTopK());
    }

    public List<Document> searchResolutionCases(String query, String warehouseId, int topK) {
        FilterExpressionBuilder filters = new FilterExpressionBuilder();
        return search(query, topK, properties.rag().caseSimilarityThreshold(), filters.and(
                filters.eq("documentType", "RESOLUTION_CASE"),
                filters.eq("warehouseId", warehouseId)).build());
    }

    private List<Document> search(
            String query,
            int topK,
            Filter.Expression filter) {
        return search(query, topK, properties.rag().similarityThreshold(), filter);
    }

    private List<Document> search(
            String query,
            int topK,
            double similarityThreshold,
            Filter.Expression filter) {
        return vectorStore.similaritySearch(SearchRequest.builder()
                .query(query)
                .topK(topK)
                .similarityThreshold(similarityThreshold)
                .filterExpression(filter)
                .build());
    }
}
