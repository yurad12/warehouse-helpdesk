package com.skala.warehouse_helpdesk.dto;

public record HoldTicketRequest(
    String palletId,
    String reason
) {
}
