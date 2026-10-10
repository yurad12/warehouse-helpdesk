package com.skala.warehouse_helpdesk.tool;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import com.skala.warehouse_helpdesk.config.WarehouseAiProperties;
import com.skala.warehouse_helpdesk.domain.Pallet;
import com.skala.warehouse_helpdesk.service.PalletService;

@Component
public class PalletTools {
    
    private final WarehouseAiProperties properties;
    private final PalletService palletService;

    public PalletTools(WarehouseAiProperties properties, PalletService palletService) {
        this.properties = properties;
        this.palletService = palletService;
    }

    @Tool(description = "내부 WMS에서 접근 가능한 Pallet의 현재 위치와 상태를 조회한다.")
    public Pallet getPalletStatus(
            @ToolParam(description = "조회할 팔레트 ID. 예: PALLET-123") String palletId) {
        return palletService.getPallet(palletId, properties.testUser().warehouseId());
    }
}
