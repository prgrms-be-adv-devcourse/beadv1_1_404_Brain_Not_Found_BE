package com.ll.products.domain.product.model.entity;

import com.ll.core.model.persistence.BaseEntity;
import com.ll.core.model.vo.kafka.enums.InventoryEventType;
import com.fasterxml.uuid.Generators;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "inventory_history")
public class InventoryHistory extends BaseEntity {

    @Column(nullable = false, name = "product_code")
    private String productCode;

    @Column(nullable = false)
    private Integer quantity; // 양수: 복구, 음수: 차감

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "transaction_type")
    private InventoryEventType transactionType; // STOCK_DECREMENT, STOCK_ROLLBACK

    @Column(name = "order_code")
    private String orderCode; // 주문 코드 (nullable - 재고 수동 조정 시)

    @Column(name = "order_status")
    private String orderStatus; // 주문 상태 (nullable)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private InventoryHistoryStatus status = InventoryHistoryStatus.PENDING;

    @Column(nullable = false, name = "reference_code")
    private String referenceCode; // 이벤트 참조 코드 (중복 방지)

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "before_quantity")
    private Integer beforeQuantity; // 처리 전 재고

    @Column(name = "after_quantity")
    private Integer afterQuantity; // 처리 후 재고

    public void markAsSuccess(Integer beforeQuantity, Integer afterQuantity) {
        this.status = InventoryHistoryStatus.SUCCESS;
        this.processedAt = LocalDateTime.now();
        this.beforeQuantity = beforeQuantity;
        this.afterQuantity = afterQuantity;
    }

    public void markAsFailed(String errorMessage) {
        this.status = InventoryHistoryStatus.FAILED;
        this.processedAt = LocalDateTime.now();
        this.errorMessage = errorMessage;
    }

    @PrePersist // save 호출하면 실행돼서 코드 생성 -> 이후 DB 저장
    private void ensureReferenceCode() {
        if (this.referenceCode == null || this.referenceCode.isBlank()) {
            this.referenceCode = Generators.timeBasedEpochGenerator().generate().toString();
        }
    }

    public void setReferenceCode(String referenceCode) {
        this.referenceCode = referenceCode;
    }
}

