package com.ll.payment.client;

import com.ll.core.model.response.BaseResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
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

    public String getOrderCodeById(Long orderId) {
        if (orderId == null) {
            throw new IllegalArgumentException("주문 ID가 필요합니다.");
        }
        try {
            BaseResponse<Map<String, String>> response = restClient.get()
                    .uri(orderServiceUrl + "/api/orders/{orderId}/code", orderId)
                    .retrieve()
                    .body(new ParameterizedTypeReference<BaseResponse<Map<String, String>>>() {});
            if (response != null && response.getData() != null) {
                return response.getData().get("orderCode");
            }
            throw new IllegalStateException("주문 코드 조회 응답이 비어있습니다. orderId: " + orderId);
        } catch (Exception e) {
            throw new IllegalStateException("주문 코드 조회 실패 - orderId: " + orderId, e);
        }
    }
}

