package com.ll.payment.model.vo.response;

import com.fasterxml.jackson.annotation.JsonFormat;
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
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss[.SSS][XXX]", timezone = "Asia/Seoul")
        LocalDateTime approvedAt,
        @JsonProperty("orderId")
        String orderId,
        @JsonProperty("method")
        String method
) {
}
