package com.skala.warehouse_helpdesk.domain;

import java.time.Instant;

public record Ticket(
        String ticketNo,
        String palletId,
        String warehouseId,
        String reason,
        String status,
        String resolutionSummary,
        String actionTaken,
        String resolvedBy,
        Instant resolvedAt,
        boolean reusable
) {
}
