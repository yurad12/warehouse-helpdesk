package com.skala.warehouse_helpdesk.external.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record RegisteredWarehouse(

        @JsonProperty("STORAGE_ITEM") String storageItem,

        @JsonProperty("RNUM") int rnum,

        @JsonProperty("COMPANY_NAME") String companyName,

        @JsonProperty("WARE_NO") String warehouseNo

) {
}
