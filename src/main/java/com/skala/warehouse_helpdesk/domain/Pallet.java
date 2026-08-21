package com.skala.warehouse_helpdesk.domain;

public record Pallet(
    String id,
    String warehouseId,
    String location,
    String status
) {
}
