package com.example.settlement.model.entity;


import com.example.core.model.persistence.BaseEntity;
import com.example.settlement.model.exception.SettlementStateTransitionException;
import com.example.settlement.model.vo.SettlementStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Entity
@Getter
@ToString
@Table(name = "settlements")
@NoArgsConstructor( access = AccessLevel.PROTECTED )
public class Settlement extends BaseEntity {

    @Column( nullable = false )
    private String sellerCode;

    @Column( nullable = false )
    private String buyerCode;

    @Column( nullable = false )
    private String orderItemCode;

    @Column( nullable = false )
    @Enumerated(EnumType.STRING)
    private SettlementStatus settlementStatus;

    @Column( nullable = false )
    private Long totalAmount;

    @Column( nullable = false )
    private BigDecimal settlementRate;

    @Column( nullable = false )
    private Long settlementCommission;

    @Column( nullable = false )
    private Long settlementBalance;

    private LocalDateTime settlementDate;

    @Builder
    public Settlement(String sellerCode, String buyerCode, String orderItemCode, SettlementStatus settlementStatus, Long totalAmount, BigDecimal settlementRate) {
        this.sellerCode = sellerCode;
        this.buyerCode = buyerCode;
        this.orderItemCode = orderItemCode;
        this.settlementStatus = settlementStatus;
        this.totalAmount = totalAmount;
        this.settlementRate = settlementRate;
    }

    public static Settlement create(String sellerCode, String buyerCode, String orderItemCode, Long totalAmount, BigDecimal settlementRate) {
        Settlement settlement = Settlement.builder()
                .sellerCode(sellerCode)
                .buyerCode(buyerCode)
                .orderItemCode(orderItemCode)
                .settlementStatus(SettlementStatus.CREATED)
                .totalAmount(totalAmount)
                .settlementRate(settlementRate)
                .build();

        settlement.process();

        return settlement;
    }

    public void done() {
        if (this.settlementStatus != SettlementStatus.CREATED) {
            throw new SettlementStateTransitionException("이미 완료된 정산입니다.");
        }
        this.settlementStatus = SettlementStatus.SUCCESS;
        this.settlementDate = LocalDateTime.now();
    }

    public void fail() {
        if (this.settlementStatus != SettlementStatus.CREATED) {
            throw new SettlementStateTransitionException("이미 실패처리된 정산입니다.");
        }
        this.settlementStatus = SettlementStatus.FAILED;
    }

    public Long calculateSettlementCommission() {
        return BigDecimal.valueOf(this.totalAmount)
                .multiply(this.settlementRate)
                .setScale(0, RoundingMode.DOWN)
                .longValue();
    }

    public Long calculateSettlementBalance(Long commission) {
        return this.totalAmount - commission;
    }

    public void process() {
        Long commission = calculateSettlementCommission();
        Long balance = calculateSettlementBalance(commission);
        this.settlementCommission = commission;
        this.settlementBalance = balance;
    }


}
