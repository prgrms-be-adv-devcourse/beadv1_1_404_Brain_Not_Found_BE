package com.ll.order.domain.client;

import com.ll.core.model.response.BaseResponse;
import com.ll.order.domain.model.vo.request.OrderPaymentRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class PaymentServiceClient {

    private final RestClient restClient;

    @Value("${external.payment-service.url:http://localhost:8084}")
    private String paymentServiceUrl;

    public String requestDepositPayment(OrderPaymentRequest request) {
        String url = paymentServiceUrl + "/api/payments/deposit";
        return restClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(String.class);
    }

    public String requestTossPayment(OrderPaymentRequest request) {
        String url = paymentServiceUrl + "/api/payments/toss";
        return restClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(String.class);
    }

    public String createPayment(Long orderId, String orderName, String customerName, Integer amount) {
        String url = paymentServiceUrl + "/api/payments/create";
        Map<String, Object> request = new HashMap<>();
        request.put("orderId", orderId);
        request.put("orderName", orderName);
        request.put("customerName", customerName);
        request.put("amount", amount);
        
        BaseResponse<String> response = restClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(new ParameterizedTypeReference<BaseResponse<String>>() {});
        
        return response != null ? response.getData() : null;
    }
}

