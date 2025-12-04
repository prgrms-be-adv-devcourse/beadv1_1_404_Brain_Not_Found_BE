package com.ll.payment.payment.model.vo.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;

public record TossPaymentResponse(
        @JsonProperty("mId")
        String mId,
        @JsonProperty("lastTransactionKey")
        String lastTransactionKey,
        @JsonProperty("paymentKey")
        String paymentKey,
        @JsonProperty("orderId")
        String orderId,
        @JsonProperty("orderName")
        String orderName,
        @JsonProperty("totalAmount")
        Integer totalAmount,
        @JsonProperty("balanceAmount")
        Integer balanceAmount,
        @JsonProperty("suppliedAmount")
        Integer suppliedAmount,
        @JsonProperty("vat")
        Integer vat,
        @JsonProperty("taxFreeAmount")
        Integer taxFreeAmount,
        @JsonProperty("status")
        String status,
        @JsonProperty("requestedAt")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss[.SSS][XXX]")
        OffsetDateTime requestedAt,
        @JsonProperty("approvedAt")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss[.SSS][XXX]")
        OffsetDateTime approvedAt,
        @JsonProperty("useEscrow")
        Boolean useEscrow,
        @JsonProperty("cultureExpense")
        Boolean cultureExpense,
        @JsonProperty("easyPay")
        EasyPay easyPay,
        @JsonProperty("receipt")
        Receipt receipt,
        @JsonProperty("checkout")
        Checkout checkout,
        @JsonProperty("currency")
        String currency,
        @JsonProperty("method")
        String method,
        @JsonProperty("type")
        String type
) {
    public record EasyPay(
            @JsonProperty("provider")
            String provider,
            @JsonProperty("amount")
            Integer amount,
            @JsonProperty("discountAmount")
            Integer discountAmount
    ) {}

    public record Receipt(
            @JsonProperty("url")
            String url
    ) {}

    public record Checkout(
            @JsonProperty("url")
            String url
    ) {}
}
