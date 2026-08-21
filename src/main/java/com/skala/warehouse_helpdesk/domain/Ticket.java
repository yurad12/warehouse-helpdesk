package com.skala.warehouse_helpdesk.domain;

public record Ticket(
    String ticketNo,
    String palletId,
    String warehouseId,
    String reason,
    String status
) {
}
