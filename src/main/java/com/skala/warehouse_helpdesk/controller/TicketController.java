package com.skala.warehouse_helpdesk.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.skala.warehouse_helpdesk.domain.Ticket;
import com.skala.warehouse_helpdesk.dto.HoldTicketRequest;
import com.skala.warehouse_helpdesk.service.TicketService;


@RestController
@RequestMapping("/api/tickets")
public class TicketController {
  
    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @PostMapping("/hold")
    public Ticket createHoldTicket(@RequestBody HoldTicketRequest request) {
        String warehouseId = "warehouse-A";

        return ticketService.createHoldTicket(request.palletId(), warehouseId, request.reason());
    }
}
