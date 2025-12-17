package com.ll.payment.settlement.model.entity;

import com.ll.core.model.persistence.BaseEntity;
import com.ll.payment.settlement.model.exception.RefundPeriodExpiredException;
import com.ll.payment.settlement.model.exception.SettlementStateTransitionException;
import com.ll.payment.settlement.model.vo.SettlementStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;

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

    @Column( name = "product_name", nullable = false )
    private String productName;

    @Column( name = "reference_code", nullable = false )
    private String referenceCode;

    @Column( name = "settlement_status", nullable = false )
    @Enumerated(EnumType.STRING)
    private SettlementStatus settlementStatus;

    @Column( name = "total_amount", nullable = false )
    @Setter
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
    public Settlement(String sellerCode, String buyerCode, String orderItemCode, String productName, String referenceCode, SettlementStatus settlementStatus, Long totalAmount, BigDecimal settlementRate) {
        this.sellerCode = sellerCode;
        this.buyerCode = buyerCode;
        this.orderItemCode = orderItemCode;
        this.productName = productName;
        this.referenceCode = referenceCode;
        this.settlementStatus = settlementStatus;
        this.totalAmount = totalAmount;
        this.settlementRate = settlementRate;
    }

    public static Settlement create(String sellerCode, String buyerCode, String orderItemCode, String productName, String referenceCode, Long totalAmount, BigDecimal settlementRate) {
        Settlement settlement = Settlement.builder()
                .sellerCode(sellerCode)
                .buyerCode(buyerCode)
                .orderItemCode(orderItemCode)
                .productName(productName)
                .referenceCode(referenceCode)
                .settlementStatus(SettlementStatus.CREATED)
                .totalAmount(totalAmount)
                .settlementRate(settlementRate)
                .build();

        settlement.process();

        return settlement;
    }

    public void done() {
        this.settlementStatus = SettlementStatus.SUCCESS;
        this.settlementDate = LocalDateTime.now();
    }

    public void fail(String errorMessage) {
        this.settlementStatus = SettlementStatus.FAILED;
        this.errorMessage = errorMessage;
        this.errorDate = LocalDateTime.now();
    }

    public void refund() {
        validateStateTransition();
        validateRefundDate();
        this.settlementStatus = SettlementStatus.REFUNDED;
    }

    public Long calculateSettlementBalance() {
        return BigDecimal.valueOf(this.totalAmount)
                .multiply(this.settlementRate)
                .setScale(0, RoundingMode.DOWN)
                .longValue();
    }

    public Long calculateSettlementCommission(Long commission) {
        return this.totalAmount - commission;
    }

    public void process() {
        Long balance = calculateSettlementBalance();
        Long commission = calculateSettlementCommission(balance);
        this.settlementCommission = commission;
        this.settlementBalance = balance;
    }

    private void validateStateTransition() {
        if (this.settlementStatus != SettlementStatus.CREATED) {
            throw new SettlementStateTransitionException();
        }
    }

    /*
        Settlement 는 OutBox 패턴으로 인해 매 5분마다 처리
        -> 환불 가능 기간을 3분 앞당겨 계산 함으로서 23:55 분 이후 환불 요청에 대한 컷오프 보정을 진행
        Dlq 정책상 재시도가 가능하기 때문에, 완전히 실패할 때 까지 3분의 여유를 둠
    */
    private void validateRefundDate() {
        final Duration REFUND_CUTOFF_GRACE_PERIOD = Duration.ofMinutes(3);

        YearMonth currentMonth = YearMonth.now(ZoneId.of("Asia/Seoul"));
        YearMonth orderMonth = YearMonth.from(this.getCreatedAt().minus(REFUND_CUTOFF_GRACE_PERIOD));

        if (currentMonth.isAfter(orderMonth)) {
            throw new RefundPeriodExpiredException();
        }
    }

}
