package com.skala.warehouse_helpdesk.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.skala.warehouse_helpdesk.domain.Pallet;
import com.skala.warehouse_helpdesk.service.PalletService;

@RestController
@RequestMapping("/api/pallets")
public class PalletController {

    private final PalletService palletService;

    public PalletController(PalletService palletService) {
        this.palletService = palletService;
    }

    @GetMapping("/{palletId}")
    public Pallet getPallet(@PathVariable String palletId, @RequestParam String warehouseId) {
        return palletService.getPallet(palletId, warehouseId);
    }
}
