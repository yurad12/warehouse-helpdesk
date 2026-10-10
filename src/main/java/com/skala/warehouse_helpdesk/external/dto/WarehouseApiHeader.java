package com.skala.warehouse_helpdesk.external.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record WarehouseApiHeader(
                String resultMsg,

                String title,

                @JsonProperty("TotalCount") int totalCount,

                @JsonProperty("RequestDate") String requestDate,

                String pageNo,

                @JsonProperty("ResultCode") int resultCode,

                @JsonProperty("TotalPages") int TotalPages,

                String numOfRows) {
}
