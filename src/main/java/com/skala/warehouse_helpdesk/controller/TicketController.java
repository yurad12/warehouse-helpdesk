package com.skala.warehouse_helpdesk.controller;

import java.util.Map;
import java.util.NoSuchElementException;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.skala.warehouse_helpdesk.config.WarehouseAiProperties;
import com.skala.warehouse_helpdesk.domain.Ticket;
import com.skala.warehouse_helpdesk.dto.HoldTicketRequest;
import com.skala.warehouse_helpdesk.dto.ResolveTicketRequest;
import com.skala.warehouse_helpdesk.service.TicketService;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final WarehouseAiProperties properties;
    private final TicketService ticketService;

    public TicketController(WarehouseAiProperties properties, TicketService ticketService) {
        this.properties = properties;
        this.ticketService = ticketService;
    }

    @PostMapping("/hold")
    public Ticket createHoldTicket(@RequestBody HoldTicketRequest request) {
        return ticketService.createHoldTicket(
                request.palletId(),
                properties.testUser().warehouseId(),
                request.reason());
    }

    @PatchMapping("/{ticketNo}/resolve")
    public Ticket resolveTicket(
            @PathVariable String ticketNo,
            @RequestBody ResolveTicketRequest request) {
        return ticketService.resolveTicket(
                ticketNo,
                properties.testUser().warehouseId(),
                request.resolutionSummary(),
                request.actionTaken(),
                properties.testUser().userId(),
                request.reusable());
    }

    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleNotFound(NoSuchElementException exception) {
        return Map.of("message", exception.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleBadRequest(IllegalArgumentException exception) {
        return Map.of("message", exception.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, String> handleConflict(IllegalStateException exception) {
        return Map.of("message", exception.getMessage());
    }
}
