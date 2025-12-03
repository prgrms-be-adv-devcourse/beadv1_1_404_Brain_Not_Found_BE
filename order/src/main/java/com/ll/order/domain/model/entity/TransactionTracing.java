package com.ll.order.domain.model.entity;

import com.ll.core.model.persistence.BaseEntity;
import com.ll.order.domain.model.enums.transaction.CompensationStatus;
import com.ll.order.domain.model.enums.transaction.TransactionStatus;
import com.ll.order.domain.model.enums.transaction.TransactionStep;
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
@Table(name = "distributed_transactions")
public class TransactionTracing extends BaseEntity {

    @Column(nullable = false, name = "order_code", unique = true)
    private String orderCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "status")
    @Builder.Default
    private TransactionStatus status = TransactionStatus.IN_PROGRESS;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_step")
    private TransactionStep currentStep;

    @Enumerated(EnumType.STRING)
    @Column(name = "compensation_status")
    @Builder.Default
    private CompensationStatus compensationStatus = CompensationStatus.NONE;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "compensation_retry_count")
    @Builder.Default
    private Integer compensationRetryCount = 0;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "failed_at")
    private LocalDateTime failedAt;

    @Column(name = "compensation_started_at")
    private LocalDateTime compensationStartedAt;

    @Column(name = "compensation_completed_at")
    private LocalDateTime compensationCompletedAt;

    public void startCompensation() {
        this.status = TransactionStatus.COMPENSATING;
        this.compensationStatus = CompensationStatus.IN_PROGRESS;
        this.compensationStartedAt = LocalDateTime.now();
    }

    public void markCompensationCompleted() {
        this.compensationStatus = CompensationStatus.COMPLETED;
        this.compensationCompletedAt = LocalDateTime.now();
    }

    public void markCompensationFailed(String errorMessage) {
        this.compensationStatus = CompensationStatus.FAILED;
        this.errorMessage = errorMessage;
        this.compensationRetryCount++;
    }
}

