package com.ll.payment.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ll.payment.model.vo.TossPaymentRequest;
import com.ll.payment.repository.PaymentJpaRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

@Service
public class PaymentServiceImpl implements  PaymentService {

    private final PaymentJpaRepository paymentJpaRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper om;

    @Value("${payment.secretKey}")
    private String secretKey;
    @Value("${payment.targetUrl}")
    private String targetUrl;

    public PaymentServiceImpl(PaymentJpaRepository paymentJpaRepository, RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.paymentJpaRepository = paymentJpaRepository;
        this.restTemplate = restTemplate;
        this.om = objectMapper;
    }

    @Override
    public String confirmPayment(TossPaymentRequest request) {
        // api 결제 승인 요청
        String url = "https://api.tosspayments.com/v1/payments/confirm";
        String target = secretKey + ":";
        Base64.Encoder encoder = Base64.getEncoder();
        String encryptedSecretKey = "Basic " + encoder.encodeToString(target.getBytes(StandardCharsets.UTF_8));

        // 헤더 추가
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON); // Content-Type: application/json
        httpHeaders.set("Authorization", encryptedSecretKey);

        Map<String, Object> requestMap = om.convertValue(request, new TypeReference<>() {
        });

        HttpEntity<Map<String, Object>> httpEntity = new HttpEntity<>(requestMap, httpHeaders);

        return restTemplate.exchange(
                        targetUrl,
                        HttpMethod.POST,
                        httpEntity,
                        String.class
                )
                .getBody();
    }
}
