package com.skala.warehouse_helpdesk.external.dto;

import java.util.List;

public record WarehouseListResponse(
                List<RegisteredWarehouse> items,
                WarehouseApiHeader header) {
}
