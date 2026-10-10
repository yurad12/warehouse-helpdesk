package com.skala.warehouse_helpdesk.tool;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import com.skala.warehouse_helpdesk.config.WarehouseAiProperties;
import com.skala.warehouse_helpdesk.domain.Ticket;
import com.skala.warehouse_helpdesk.service.TicketService;

@Component
public class TicketTools {
    
    private final WarehouseAiProperties properties;
    private final TicketService ticketService;

    public TicketTools(WarehouseAiProperties properties, TicketService ticketService) {
        this.properties = properties;
        this.ticketService = ticketService;
    }

    @Tool(description = "내부 Pallet의 출고 보류 요청을 생성한다. 실제 Pallet 상태는 변경하지 않고 PENDING 티켓만 생성한다.")
    public Ticket createHoldTicket(
            @ToolParam(description = "출고 보류 요청 대상 팔레트 ID. 예: PALLET-123") String palletId,
            @ToolParam(description = "출고 보류 사유. 예: 파손 의심") String reason) {
        return ticketService.createHoldTicket(palletId, properties.testUser().warehouseId(), reason);
    }
}
