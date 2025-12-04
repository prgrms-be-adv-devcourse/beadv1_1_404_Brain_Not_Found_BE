package com.ll.payment.payment.model.vo.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TossPaymentCreateResponse(
        @JsonProperty("paymentKey")
        String paymentKey,
        @JsonProperty("orderId")
        String orderId,
        @JsonProperty("status")
        String status,
        @JsonProperty("checkout")
        Checkout checkout
) {
    public record Checkout(
            @JsonProperty("url")
            String url
    ) {
    }
}

