package com.ll.order.domain.model.vo.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record OrderValidateRequest(
        @NotBlank(message = "구매자 코드는 필수입니다.")
        String buyerCode,

        @NotEmpty(message = "검증할 상품 정보가 필요합니다.")
        List<@Valid ProductRequest> products
) {
}
