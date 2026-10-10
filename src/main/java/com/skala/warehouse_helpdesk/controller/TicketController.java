package com.skala.warehouse_helpdesk.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.skala.warehouse_helpdesk.config.WarehouseAiProperties;
import com.skala.warehouse_helpdesk.domain.Ticket;
import com.skala.warehouse_helpdesk.dto.HoldTicketRequest;
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
}
