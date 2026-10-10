package com.skala.warehouse_helpdesk.tool;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import com.skala.warehouse_helpdesk.external.dto.WarehouseListResponse;
import com.skala.warehouse_helpdesk.service.WarehouseRegistryService;

@Component
public class WarehouseRegistryTools {
    
    private final WarehouseRegistryService warehouseRegistryService;

    public WarehouseRegistryTools(WarehouseRegistryService warehouseRegistryService) {
        this.warehouseRegistryService = warehouseRegistryService;
    }

    @Tool(description = "국토교통부 OpenAPI에서 외부 물류창고업 등록정보를 조회한다.")
    public WarehouseListResponse searchRegisteredWarehouses(
            @ToolParam(description = "조회 시작일. yyyyMMdd 형식. 예: 20260822") String startDate,
            @ToolParam(description = "조회 종료일. yyyyMMdd 형식. 예: 20260822") String endDate) {
        return warehouseRegistryService.searchWarehouses(startDate, endDate);
    }
}
