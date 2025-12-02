package com.ll.products.domain.product.model.entity;

import com.ll.core.model.persistence.BaseEntity;
import com.ll.core.model.vo.kafka.enums.InventoryEventType;
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
@Table(name = "inventory_dlq_events")
public class InventoryDlqEvent extends BaseEntity {

    @Column(nullable = false)
    private String productCode;

    @Column(nullable = false)
    private Integer quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "event_type")
    private InventoryEventType eventType;

    @Column(nullable = false, name = "reference_code")
    private String referenceCode;

    @Column(nullable = false, name = "retry_count")
    @Builder.Default
    private Integer retryCount = 5; // 이미 5번 재시도했음

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private DlqStatus status = DlqStatus.PENDING; // 수동 처리 대기

    @Column(name = "failed_at", nullable = false)
    @Builder.Default
    private LocalDateTime failedAt = LocalDateTime.now();

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(name = "processed_by")
    private String processedBy;

    public void processed() {
        this.status = DlqStatus.PROCESSED;
        this.processedAt = LocalDateTime.now();
    }

}

