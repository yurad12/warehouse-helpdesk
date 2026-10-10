package com.skala.warehouse_helpdesk.service;

import java.time.Instant;
import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.skala.warehouse_helpdesk.domain.Ticket;
import com.skala.warehouse_helpdesk.repository.PalletRepository;
import com.skala.warehouse_helpdesk.repository.TicketRepository;

@Service
public class TicketService {

    private static final Logger log = LoggerFactory.getLogger(TicketService.class);

    private final PalletRepository palletRepository;
    private final TicketRepository ticketRepository;

    public TicketService(PalletRepository palletRepository, TicketRepository ticketRepository) {
        this.palletRepository = palletRepository;
        this.ticketRepository = ticketRepository;
    }

    public Ticket createHoldTicket(
            String palletId,
            String warehouseId,
            String reason) {
        palletRepository
                .findByIdAndWarehouseId(palletId, warehouseId)
                .orElseThrow(() -> new IllegalArgumentException("해당 팔레트를 찾을 수 없습니다."));

        String normalizedReason = normalizeReason(reason);

        return ticketRepository
                .findPendingTicket(palletId, warehouseId, normalizedReason)
                .orElseGet(() -> ticketRepository.save(palletId, warehouseId, normalizedReason));
    }

    public Ticket resolveTicket(
            String ticketNo,
            String warehouseId,
            String resolutionSummary,
            String actionTaken,
            String resolvedBy,
            boolean reusable) {
        try {
            Ticket ticket = ticketRepository.findByTicketNo(ticketNo);

            if (ticket == null || !ticket.warehouseId().equals(warehouseId)) {
                throw new NoSuchElementException("해당 티켓을 찾을 수 없습니다.");
            }
            if (!ticket.status().equals("PENDING")) {
                throw new IllegalStateException("이미 처리 완료된 티켓입니다.");
            }

            String normalizedSummary = requireText(resolutionSummary, "처리 결과 요약");
            String normalizedAction = requireText(actionTaken, "조치 내용");
            String normalizedResolvedBy = requireText(resolvedBy, "처리 담당자");

            Ticket resolvedTicket = ticketRepository.resolve(
                    ticket,
                    normalizedSummary,
                    normalizedAction,
                    normalizedResolvedBy,
                    Instant.now(),
                    reusable);

            log.info("event=ticket_resolution ticketNo={} warehouseId={} resolvedBy={} reusable={} status=OK",
                    ticketNo,
                    warehouseId,
                    normalizedResolvedBy,
                    reusable);
            return resolvedTicket;
        }
        catch (RuntimeException exception) {
            log.warn("event=ticket_resolution ticketNo={} warehouseId={} resolvedBy={} reusable={} status=FAIL reason={}",
                    ticketNo,
                    warehouseId,
                    resolvedBy,
                    reusable,
                    exception.getClass().getSimpleName());
            throw exception;
        }
    }

    private String normalizeReason(String reason) {
        return reason == null ? "" : reason.trim().replaceAll("\\s+", " ").toLowerCase();
    }

    private String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + "은 필수입니다.");
        }
        return value.trim().replaceAll("\\s+", " ");
    }
}
