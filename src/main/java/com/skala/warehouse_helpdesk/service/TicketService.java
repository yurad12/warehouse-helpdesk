package com.skala.warehouse_helpdesk.service;

import org.springframework.stereotype.Service;

import com.skala.warehouse_helpdesk.domain.Ticket;
import com.skala.warehouse_helpdesk.repository.PalletRepository;
import com.skala.warehouse_helpdesk.repository.TicketRepository;

@Service
public class TicketService {
    
    private final PalletRepository palletRepository;
    private final TicketRepository ticketRepository;

    public TicketService(PalletRepository palletRepository, TicketRepository ticketRepository) {
        this.palletRepository = palletRepository;
        this.ticketRepository = ticketRepository;
    }

    public Ticket createHoldTicket(
        String palletId,
        String warehouseId,
        String reason
    ) {
        palletRepository
            .findByIdAndWarehouseId(palletId, warehouseId)
            .orElseThrow(() -> new IllegalArgumentException("해당 팔레트를 찾을 수 없습니다."));
        
            return ticketRepository.save(palletId, warehouseId, reason);
    }
}
