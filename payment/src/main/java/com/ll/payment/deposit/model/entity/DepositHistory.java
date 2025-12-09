package com.ll.payment.deposit.model.entity;

import com.ll.core.model.persistence.BaseEntity;
import com.ll.payment.deposit.model.enums.DepositHistoryType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Table(name = "deposit_histories")
@NoArgsConstructor( access = AccessLevel.PROTECTED )
public class DepositHistory extends BaseEntity {

    @Setter
    @JoinColumn(name = "deposit_id", nullable = false)
    private Long depositId;

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

    @Builder
    public DepositHistory(Long depositId, Long amount, Long balanceBefore, Long balanceAfter, String referenceCode, DepositHistoryType historyType) {
        this.depositId = depositId;
        this.amount = amount;
        this.balanceBefore = balanceBefore;
        this.balanceAfter = balanceAfter;
        this.referenceCode = referenceCode;
        this.historyType = historyType;
    }

    public static DepositHistory create(Long depositId, Long amount, Long balanceBefore, Long balanceAfter, String referenceCode, DepositHistoryType historyType) {
        return DepositHistory.builder()
                .depositId(depositId)
                .amount(amount)
                .balanceBefore(balanceBefore)
                .balanceAfter(balanceAfter)
                .referenceCode(referenceCode)
                .historyType(historyType)
                .build();
    }

}
