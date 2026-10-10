package com.skala.warehouse_helpdesk.dto;

import java.util.List;

public record ChatResponse(
        String answer,
        List<ChatSource> sources,
        boolean toolUsed) {
}
