package com.skala.warehouse_helpdesk.dto;

public record AskRequest(
        String question,
        String sessionId) {
}
