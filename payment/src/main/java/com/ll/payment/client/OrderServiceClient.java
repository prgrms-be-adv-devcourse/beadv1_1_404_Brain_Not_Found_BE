package com.ll.payment.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
public class OrderServiceClient {

    private final RestClient restClient;

    @Value("${external.order-service.url:http://localhost:8082}")
    private String orderServiceUrl;

    public OrderServiceClient(RestClient restClient) {
        this.restClient = restClient;
    }

    public void updateOrderStatus(String orderCode, String status) {
        if (orderCode == null || orderCode.isBlank()) {
            throw new IllegalArgumentException("주문 코드가 필요합니다.");
        }
        restClient.patch()
                .uri(orderServiceUrl + "/api/orders/{orderCode}/status", orderCode)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("status", status))
                .retrieve()
                .toBodilessEntity();
    }
}

