package com.ll.order.domain.model.vo.request;

import com.ll.order.domain.model.enums.OrderType;

public record OrderDirectRequest(
        String userCode,
        String productCode,
        int quantity,
        String address,
        OrderType orderType
        // name: 사용자 서비스에서 조회
        // totalPrice: 상품 서비스에서 조회하여 계산
        // image: 응답에만 포함
) {
}
