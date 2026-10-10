package com.skala.warehouse_helpdesk.external;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.skala.warehouse_helpdesk.external.dto.WarehouseListResponse;

@Component
public class WarehouseRegistryClient {

    private final String baseUrl;
    private final RestClient restClient;
    private final String serviceKey;

    public WarehouseRegistryClient(
            @Value("${warehouse.public-api.base-url}") String baseUrl,
            @Value("${warehouse.public-api.service-key}") String serviceKey) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.restClient = RestClient.create();
        this.serviceKey = encodeServiceKey(serviceKey);
    }

    public WarehouseListResponse searchWarehouses(String startDate, String endDate) {
        URI uri = URI.create(baseUrl + "/WhsInfoList"
                + "?serviceKey=" + serviceKey
                + "&pageNo=1"
                + "&numOfRows=10"
                + "&type=json"
                + "&sday=" + startDate
                + "&eday=" + endDate);

        return restClient.get()
                .uri(uri)
                .retrieve()
                .body(WarehouseListResponse.class);
    }

    private String encodeServiceKey(String serviceKey) {
        String trimmedServiceKey = serviceKey.trim();

        if (trimmedServiceKey.contains("%")) {
            return trimmedServiceKey;
        }

        return URLEncoder.encode(trimmedServiceKey, StandardCharsets.UTF_8);
    }
}
