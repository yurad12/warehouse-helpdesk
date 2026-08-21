package com.skala.warehouse_helpdesk.repository;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Repository;

import com.skala.warehouse_helpdesk.domain.Ticket;

@Repository
public class TicketRepository {
    
    private final Map<String, Ticket> tickets = new LinkedHashMap<>();
    private final AtomicInteger sequence = new AtomicInteger(1000);

    public Ticket save(
        String palletId,
        String warehouseId,
        String reason
    ) {
        String ticketNo = "TK-" + sequence.incrementAndGet();

        Ticket ticket = new Ticket(
            ticketNo,
            palletId,
            warehouseId,
            reason,
            "PENDING"
        );

        tickets.put(ticketNo, ticket);

        return ticket;
    };

    public Ticket findByTicketNo(String ticketNo) {
        return tickets.get(ticketNo);
    }
}
