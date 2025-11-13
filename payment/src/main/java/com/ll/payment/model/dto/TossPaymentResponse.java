package com.ll.payment.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public record TossPaymentResponse(
        String status,
        @JsonProperty("approvedAmount")
        Integer approvedAmount,
        @JsonProperty("totalAmount")
        Integer totalAmount,
        @JsonProperty("paymentKey")
        String paymentKey,
        @JsonProperty("transactionKey")
        String transactionKey,
        @JsonProperty("approvedAt")
        LocalDateTime approvedAt,
        @JsonProperty("orderId")
        String orderId,
        @JsonProperty("method")
        String method
) {
}
