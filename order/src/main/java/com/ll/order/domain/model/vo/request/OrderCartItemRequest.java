package com.ll.order.domain.model.vo.request;

import com.ll.order.domain.model.enums.OrderType;
import com.ll.order.domain.model.enums.PaidType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

public record OrderCartItemRequest(
        @NotBlank(message = "구매자 코드가 필요합니다.")
        String buyerCode,

        @NotBlank(message = "장바구니 코드가 필요합니다.")
        String cartCode,

        @NotBlank(message = "구매자 이름이 필요합니다.")
        String name,

        @NotBlank(message = "배송지 주소가 필요합니다.")
        String address,

        @NotEmpty(message = "장바구니 상품 정보가 필요합니다.")
        List<@Valid ProductRequest> products,

        @Positive(message = "총 결제 금액은 0보다 커야 합니다.")
        int totalPrice,

        @NotNull(message = "주문 유형이 필요합니다.")
        OrderType orderType,

        @NotNull(message = "결제 수단이 필요합니다.")
        PaidType paidType,

        String paymentKey // 토스 승인용
) {
}
