package com.skala.warehouse_helpdesk.rag;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import com.skala.warehouse_helpdesk.config.WarehouseAiProperties;
import com.skala.warehouse_helpdesk.domain.Ticket;
import com.skala.warehouse_helpdesk.repository.TicketRepository;

@Service
public class ResolutionCaseIndexService {

    private static final Logger log = LoggerFactory.getLogger(ResolutionCaseIndexService.class);

    private final TicketRepository ticketRepository;
    private final VectorStore vectorStore;
    private final WarehouseAiProperties properties;

    public ResolutionCaseIndexService(
            TicketRepository ticketRepository,
            VectorStore vectorStore,
            WarehouseAiProperties properties) {
        this.ticketRepository = ticketRepository;
        this.vectorStore = vectorStore;
        this.properties = properties;
    }

    public void index(Ticket ticket) {
        if (!ticket.reusable() || !"RESOLVED".equals(ticket.status())) {
            return;
        }

        Document document = Document.builder()
                .id("resolution-case-" + ticket.ticketNo())
                .text(caseText(ticket))
                .metadata("source", ticket.ticketNo())
                .metadata("version", ticket.resolvedAt().toString())
                .metadata("documentType", "RESOLUTION_CASE")
                .metadata("warehouseId", ticket.warehouseId())
                .metadata("ticketNo", ticket.ticketNo())
                .build();

        vectorStore.add(List.of(document));
        log.info("event=resolution_case_index ticketNo={} warehouseId={} status=OK",
                ticket.ticketNo(),
                ticket.warehouseId());
    }

    public void reindexAll() {
        ticketRepository.findReusableResolvedTickets(properties.testUser().warehouseId()).forEach(ticket -> {
            try {
                index(ticket);
            }
            catch (RuntimeException exception) {
                log.warn("event=resolution_case_index ticketNo={} warehouseId={} status=FAIL reason={}",
                        ticket.ticketNo(),
                        ticket.warehouseId(),
                        exception.getClass().getSimpleName());
            }
        });
    }

    private String caseText(Ticket ticket) {
        return """
                출고 보류 처리 사례
                요청 사유: %s
                처리 결과: %s
                조치 내용: %s
                """.formatted(ticket.reason(), ticket.resolutionSummary(), ticket.actionTaken());
    }
}
