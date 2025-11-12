package com.example.deposit.model.entity;

import com.example.core.model.persistence.BaseEntity;
import com.example.deposit.model.enums.DepositHistoryType;
import com.example.deposit.model.enums.TransactionStatus;
import com.example.deposit.model.exception.InvalidDepositHistoryStatusTransitionException;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Table(name = "deposit_histories")
@NoArgsConstructor( access = AccessLevel.PROTECTED )
public class DepositHistory extends BaseEntity {

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deposit_id", nullable = false)
    private Deposit deposit;

    @Column( nullable = false )
    private Long amount;

    @Column( nullable = false )
    private Long balanceBefore;

    @Column( nullable = false )
    private Long balanceAfter;

    @Column( nullable = false, unique = true, updatable = false )
    private String referenceCode;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private DepositHistoryType historyType;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TransactionStatus transactionStatus;

    @Builder
    public DepositHistory(Deposit deposit, Long amount, Long balanceBefore, Long balanceAfter, String referenceCode, DepositHistoryType historyType, TransactionStatus transactionStatus) {
        this.deposit = deposit;
        this.amount = amount;
        this.balanceBefore = balanceBefore;
        this.balanceAfter = balanceAfter;
        this.referenceCode = referenceCode;
        this.historyType = historyType;
        this.transactionStatus = transactionStatus;
    }

    public void setTransactionCompleted() {
        if ( transactionStatus != TransactionStatus.PENDING ) {
            throw new InvalidDepositHistoryStatusTransitionException("거래 상태가 PENDING 이어야만 COMPLETED 로 변경할 수 있습니다.");
        }
        this.transactionStatus = TransactionStatus.COMPLETED;
    }

    public void setTransactionFailed() {
        if ( transactionStatus != TransactionStatus.PENDING ) {
            throw new InvalidDepositHistoryStatusTransitionException("거래 상태가 PENDING 이어야만 FAILED 로 변경할 수 있습니다.");
        }
        this.transactionStatus = TransactionStatus.FAILED;
    }

    public void setTransactionCancelled() {
        if ( transactionStatus != TransactionStatus.PENDING ) {
            throw new InvalidDepositHistoryStatusTransitionException("거래 상태가 PENDING 이어야만 CANCELLED 로 변경할 수 있습니다.");
        }
        this.transactionStatus = TransactionStatus.CANCELLED;
    }

}
