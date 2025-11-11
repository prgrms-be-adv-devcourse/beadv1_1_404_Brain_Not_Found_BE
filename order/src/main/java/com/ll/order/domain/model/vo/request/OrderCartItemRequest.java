package com.ll.order.domain.model.vo.request;

import com.ll.order.domain.model.enums.OrderType;
import com.ll.payment.model.enums.PaidType;

import java.util.List;

public record OrderCartItemRequest(
    String buyerCode,
    String cartCode,
    String name,
    String address,
    List<ProductRequest> products,
    int totalPrice,
    OrderType orderType,
    PaidType paidType,
    String paymentKey // 토스 승인용
) {
}
