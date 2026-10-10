package com.skala.warehouse_helpdesk.dto;

public record ResolveTicketRequest(
        String resolutionSummary,
        String actionTaken,
        boolean reusable) {
}
