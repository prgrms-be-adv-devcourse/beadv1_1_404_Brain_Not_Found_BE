package com.ll.order.domain.model.vo.response.product;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Builder;

@Builder
public record ProductImageDto(
        @NotBlank(message = "이미지 URL은 필수입니다")
        String url,

        @NotNull(message = "이미지 순서는 필수입니다")
        @PositiveOrZero(message = "이미지 순서는 0 이상이어야 합니다")
        Integer sequence,

        @NotNull(message = "메인 이미지 여부는 필수입니다")
        Boolean isMain
) {
}

