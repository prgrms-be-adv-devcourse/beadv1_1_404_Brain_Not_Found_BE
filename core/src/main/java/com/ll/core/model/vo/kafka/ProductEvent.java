package com.ll.core.model.vo.kafka;

import com.ll.core.model.vo.kafka.enums.ProductEventType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;

@Builder
public record ProductEvent(

        @NotNull(message = "이벤트 타입은 필수입니다.")
        ProductEventType eventType,

        @NotBlank(message = "상품코드는 필수입니다. 공백불가.")
        String productCode,

        @NotBlank(message = "상품명은 필수입니다. 공백불가.")
        String name,

        String description,

        @NotNull(message = "가격은 필수입니다")
        @Positive(message = "가격은 양수여야 합니다")
        Integer price,

        @NotNull(message = "수량은 필수입니다")
        @Positive(message = "수량은 양수여야 합니다")
        Integer quantity,

        Long categoryId,

        String categoryName,

        @NotNull(message = "상품 상태는 필수입니다.")
        String status,

        String mainImageFileKey,

        @NotNull(message = "생성일은 필수입니다.")
        String createdAt,

        String updatedAt
) {
}
