package com.ll.order.domain.model.vo.request;

import java.util.List;

public record OrderValidateRequest(
        String userCode,
        String orderCode,
        List<ProductRequest> product,
        int totalPrice,
        String address
) {
}
