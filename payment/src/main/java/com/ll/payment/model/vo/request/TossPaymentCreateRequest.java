package com.ll.payment.model.vo.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TossPaymentCreateRequest(
        @JsonProperty("amount")
        Integer amount,
        @JsonProperty("orderId")
        String orderId,
        @JsonProperty("orderName")
        String orderName,
        @JsonProperty("customerName")
        String customerName,
        @JsonProperty("successUrl")
        String successUrl,
        @JsonProperty("failUrl")
        String failUrl
) {
}

