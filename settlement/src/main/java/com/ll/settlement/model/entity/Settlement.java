package com.ll.settlement.model.entity;


import com.ll.core.model.persistence.BaseEntity;
import com.ll.settlement.model.exception.SettlementStateTransitionException;
import com.ll.settlement.model.vo.SettlementStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Entity
@Getter
@ToString
@Table(
        name = "settlements",
        indexes = {
                @Index(name = "idx_settlement_query", columnList = "settlement_status, settlement_date, created_at")
        }
)
@NoArgsConstructor( access = AccessLevel.PROTECTED )
public class Settlement extends BaseEntity {

    @Column( name = "seller_code", nullable = false )
    private String sellerCode;

    @Column( name = "buyer_code", nullable = false )
    private String buyerCode;

    @Column( name = "order_item_code", nullable = false )
    private String orderItemCode;

    @Column( name = "reference_code", nullable = false )
    private String referenceCode;

    @Column( name = "settlement_status", nullable = false )
    @Enumerated(EnumType.STRING)
    private SettlementStatus settlementStatus;

    @Column( name = "total_amount", nullable = false )
    private Long totalAmount;

    @Column( name = "settlement_rate", nullable = false )
    private BigDecimal settlementRate;

    @Column( name = "settlement_commission", nullable = false )
    private Long settlementCommission;

    @Column( name = "settlement_balance", nullable = false )
    private Long settlementBalance;

    @Column( name = "error_message" )
    private String errorMessage;

    @Column( name = "settlement_date" )
    private LocalDateTime settlementDate;

    @Column( name = "error_date" )
    private LocalDateTime errorDate;

    @Builder
    public Settlement(String sellerCode, String buyerCode, String orderItemCode, String referenceCode, SettlementStatus settlementStatus, Long totalAmount, BigDecimal settlementRate) {
        this.sellerCode = sellerCode;
        this.buyerCode = buyerCode;
        this.orderItemCode = orderItemCode;
        this.referenceCode = referenceCode;
        this.settlementStatus = settlementStatus;
        this.totalAmount = totalAmount;
        this.settlementRate = settlementRate;
    }

    public static Settlement create(String sellerCode, String buyerCode, String orderItemCode, String referenceCode, Long totalAmount, BigDecimal settlementRate) {
        Settlement settlement = Settlement.builder()
                .sellerCode(sellerCode)
                .buyerCode(buyerCode)
                .orderItemCode(orderItemCode)
                .referenceCode(referenceCode)
                .settlementStatus(SettlementStatus.CREATED)
                .totalAmount(totalAmount)
                .settlementRate(settlementRate)
                .build();

        settlement.process();

        return settlement;
    }

    public void done() {
        if (this.settlementStatus != SettlementStatus.CREATED) {
            throw new SettlementStateTransitionException("SUCCESS 상태로 전환할 수 없습니다.");
        }
        this.settlementStatus = SettlementStatus.SUCCESS;
        this.settlementDate = LocalDateTime.now();
    }

    public void fail(String errorMessage) {
        this.settlementStatus = SettlementStatus.FAILED;
        this.errorMessage = errorMessage;
        this.errorDate = LocalDateTime.now();
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
