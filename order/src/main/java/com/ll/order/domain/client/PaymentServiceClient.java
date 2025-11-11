package com.ll.order.domain.client;

import com.ll.order.domain.model.vo.request.OrderPaymentRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class PaymentServiceClient {

    private final RestTemplate restTemplate;

    @Value("${external.payment-service.url:http://localhost:8084}")
    private String paymentServiceUrl;

    public String requestDepositPayment(OrderPaymentRequest request) {
        String url = paymentServiceUrl + "/api/payments/deposit";
        return restTemplate.postForObject(url, createHttpEntity(request), String.class);
    }

    public String requestTossPayment(OrderPaymentRequest request) {
        String url = paymentServiceUrl + "/api/payments/toss";
        return restTemplate.postForObject(url, createHttpEntity(request), String.class);
    }

    private HttpEntity<OrderPaymentRequest> createHttpEntity(OrderPaymentRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(request, headers);
    }
}

