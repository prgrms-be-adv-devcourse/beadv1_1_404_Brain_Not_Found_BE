package com.ll.products.domain.product.model.entity;

import com.ll.core.model.persistence.BaseEntity;
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
@Table(name = "product_dlq_event")
public class ProductDlqEvent extends BaseEntity {

    @Column(nullable = false)
    private String topic;

    @Column(name = "event_payload", nullable = false, columnDefinition = "TEXT")
    private String eventPayload;

    @Column(name = "failed_at")
    private LocalDateTime failedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private DlqStatus status = DlqStatus.PENDING;

}