package com.ll.order.domain.model.vo.request;

import com.ll.order.domain.model.enums.OrderType;
import com.ll.order.domain.model.enums.PaidType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record OrderDirectRequest(
        @NotBlank(message = "사용자 코드가 필요합니다.")
        String userCode,

        @NotBlank(message = "상품 코드가 필요합니다.")
        String productCode,

        @Positive(message = "주문 수량은 1 이상이어야 합니다.")
        int quantity,

        @NotBlank(message = "배송지 주소가 필요합니다.")
        String address,

        @NotNull(message = "주문 유형이 필요합니다.")
        OrderType orderType,

        @NotNull(message = "결제 수단이 필요합니다.")
        PaidType paidType,

        String paymentKey // 토스 승인용
        // name: 사용자 서비스에서 조회
        // totalPrice: 상품 서비스에서 조회하여 계산
        // image: 응답에만 포함
) {
}
