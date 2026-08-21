package com.skala.warehouse_helpdesk.service;

import org.springframework.stereotype.Service;

import com.skala.warehouse_helpdesk.domain.Pallet;
import com.skala.warehouse_helpdesk.repository.PalletRepository;

@Service
public class PalletService {
    
    private final PalletRepository palletRepository;

    public PalletService(PalletRepository palletRepository) {
        this.palletRepository = palletRepository;
    }

    public Pallet getPallet(String palletId, String warehouseId) {
        return palletRepository
                .findByIdAndWarehouseId(palletId, warehouseId)
                .orElseThrow(() -> new IllegalArgumentException("팔레트를 찾을 수 없습니다."));
    }    
}
