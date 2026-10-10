package com.skala.warehouse_helpdesk.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "warehouse.ai")
public record WarehouseAiProperties(
        String provider,
        String systemPrompt,
        Rag rag,
        Memory memory,
        TestUser testUser) {

    public record Rag(
            int topK,
            int maxTopK,
            int previewLength,
            double similarityThreshold) {
    }

    public record Memory(
            int maxMessages,
            String defaultSessionId) {
    }

    public record TestUser(
            String userId,
            String warehouseId) {
    }
}
