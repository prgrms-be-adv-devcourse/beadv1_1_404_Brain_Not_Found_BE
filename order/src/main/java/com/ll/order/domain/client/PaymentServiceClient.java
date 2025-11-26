package com.ll.order.domain.client;

import com.ll.order.domain.model.vo.request.OrderPaymentRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class PaymentServiceClient {

    private final RestClient restClient;

    @Value("${external.payment-service.url:http://localhost:8087}")
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

    public void requestTossPayment(OrderPaymentRequest request) {
        String url = paymentServiceUrl + "/api/payments/toss";
        restClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(String.class);
    }

    public void requestRefund(Long orderId, String orderCode, String buyerCode, Integer refundAmount, String reason) {
        String url = paymentServiceUrl + "/api/payments/refund";
        Map<String, Object> request = new HashMap<>();
        request.put("orderId", orderId);
        request.put("orderCode", orderCode);
        request.put("buyerCode", buyerCode);
        request.put("refundAmount", refundAmount);
        request.put("reason", reason != null ? reason : "주문 취소");

        restClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(String.class);
    }

}

