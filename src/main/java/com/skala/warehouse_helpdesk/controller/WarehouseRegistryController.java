package com.skala.warehouse_helpdesk.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.skala.warehouse_helpdesk.external.dto.WarehouseListResponse;
import com.skala.warehouse_helpdesk.service.WarehouseRegistryService;

@RestController
@RequestMapping("/api/warehouses")
public class WarehouseRegistryController {

    private final WarehouseRegistryService warehouseRegistryService;

    public WarehouseRegistryController(WarehouseRegistryService warehouseRegistryService) {
        this.warehouseRegistryService = warehouseRegistryService;
    }

    @GetMapping
    public WarehouseListResponse searchWarehouses(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        return warehouseRegistryService.searchWarehouses(startDate, endDate);
    }
}
