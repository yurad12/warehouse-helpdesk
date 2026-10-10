package com.skala.warehouse_helpdesk.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.skala.warehouse_helpdesk.domain.Ticket;

@SpringBootTest
class TicketServiceTests {

    private final TicketService ticketService;

    @Autowired
    TicketServiceTests(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @Test
    void returnsExistingPendingTicketForSamePalletWarehouseAndNormalizedReason() {
        Ticket firstTicket = ticketService.createHoldTicket("PALLET-123", "warehouse-A", " 파손   의심 ");
        Ticket secondTicket = ticketService.createHoldTicket("PALLET-123", "warehouse-A", "파손 의심");

        assertThat(secondTicket.ticketNo()).isEqualTo(firstTicket.ticketNo());
        assertThat(secondTicket.status()).isEqualTo("PENDING");
    }

    @Test
    void createsDifferentTicketForDifferentReason() {
        Ticket firstTicket = ticketService.createHoldTicket("PALLET-456", "warehouse-A", "파손 의심");
        Ticket secondTicket = ticketService.createHoldTicket("PALLET-456", "warehouse-A", "라벨 훼손");

        assertThat(secondTicket.ticketNo()).isNotEqualTo(firstTicket.ticketNo());
    }
}
