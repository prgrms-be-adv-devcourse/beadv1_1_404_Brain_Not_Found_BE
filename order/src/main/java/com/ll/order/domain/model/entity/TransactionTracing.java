package com.ll.order.domain.model.entity;

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

    // 상태 변경 메서드들
    public void markInventoryDeductionStarted() {
        this.currentStep = TransactionStep.INVENTORY_DEDUCTION;
        this.status = TransactionStatus.IN_PROGRESS;
    }

    public void markInventoryDeductionCompleted() {
        this.currentStep = TransactionStep.PAYMENT;
    }

    public void markPaymentStarted() {
        this.currentStep = TransactionStep.PAYMENT;
    }

    public void markPaymentCompleted() {
        this.currentStep = TransactionStep.ORDER_COMPLETION;
    }

    public void markOrderCompletionCompleted() {
        this.status = TransactionStatus.COMPLETED;
        this.currentStep = TransactionStep.ORDER_COMPLETION;
        this.completedAt = LocalDateTime.now();
    }

    public void markAsFailed(String errorMessage) {
        this.status = TransactionStatus.FAILED;
        this.errorMessage = errorMessage;
        this.failedAt = LocalDateTime.now();
    }

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

    public void incrementCompensationRetryCount() {
        this.compensationRetryCount++;
    }

    public enum TransactionStatus {
        IN_PROGRESS,    // 진행 중
        COMPLETED,      // 완료
        FAILED,         // 실패
        COMPENSATING    // 보상 중
    }

    public enum TransactionStep {
        INVENTORY_DEDUCTION,   // 재고 차감
        PAYMENT,               // 결제
        ORDER_COMPLETION       // 주문 완료
    }

    public enum CompensationStatus {
        NONE,           // 보상 없음
        IN_PROGRESS,    // 보상 진행 중
        COMPLETED,      // 보상 완료
        FAILED          // 보상 실패
    }
}

