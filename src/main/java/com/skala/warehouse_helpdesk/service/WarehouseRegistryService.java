package com.skala.warehouse_helpdesk.service;

import org.springframework.stereotype.Service;

import com.skala.warehouse_helpdesk.external.WarehouseRegistryClient;
import com.skala.warehouse_helpdesk.external.dto.WarehouseListResponse;

@Service
public class WarehouseRegistryService {

    private final WarehouseRegistryClient warehouseRegistryClient;

    public WarehouseRegistryService(WarehouseRegistryClient warehouseRegistryClient) {
        this.warehouseRegistryClient = warehouseRegistryClient;
    }

    public WarehouseListResponse searchWarehouses(String startDate, String endDate) {
        return warehouseRegistryClient.searchWarehouses(startDate, endDate);
    }
}
