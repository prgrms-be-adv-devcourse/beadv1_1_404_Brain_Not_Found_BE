package com.ll.order.domain.model.vo.request;

public record OrderCancelRequest(
    String orderCode,
    String reason,
    ProductRequest productRequest

) {
}
