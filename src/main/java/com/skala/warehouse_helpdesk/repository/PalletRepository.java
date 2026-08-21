package com.skala.warehouse_helpdesk.repository;

import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.skala.warehouse_helpdesk.domain.Pallet;

@Repository
public class PalletRepository {

    private final Map<String, Pallet> pallets = Map.of(
        "PALLET-123",
        new Pallet("PALLET-123", "warehouse-A", "A-03-02", "AVAILABLE"),

        "PALLET-456",
        new Pallet("PALLET-456", "warehouse-A", "B-01-04", "HOLD"),

        "PALLET-999",
        new Pallet("PALLET-999", "warehouse-B", "C-05-01", "AVAILABLE")
    );

    public Optional<Pallet> findByIdAndWarehouseId(
        String palletId,
        String warehouseId
    ) {
        return Optional.ofNullable(pallets.get(palletId))
                .filter(pallet -> pallet.warehouseId().equals(warehouseId));
    }
}
