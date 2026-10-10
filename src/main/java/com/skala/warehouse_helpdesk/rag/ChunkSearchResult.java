package com.skala.warehouse_helpdesk.rag;

public record ChunkSearchResult(
        String source,
        String version,
        Double score,
        String preview) {
}
