package com.ll.payment.payment.service.deposit;

import com.ll.core.model.exception.BaseException;
import com.ll.payment.deposit.model.vo.request.DepositTransactionRequest;
import com.ll.payment.deposit.model.vo.response.DepositResponse;
import com.ll.payment.deposit.service.DepositService;
import com.ll.payment.payment.exception.PaymentErrorCode;
import com.ll.payment.payment.model.entity.Payment;
import com.ll.payment.payment.model.entity.PaymentHistoryEntity;
import com.ll.payment.payment.model.enums.PaymentHistoryActionType;
import com.ll.payment.payment.model.enums.PaymentStatus;
import com.ll.payment.payment.model.vo.PaymentProcessResult;
import com.ll.payment.payment.model.vo.request.PaymentRequest;
import com.ll.payment.payment.repository.PaymentHistoryJpaRepository;
import com.ll.payment.payment.repository.PaymentJpaRepository;
import com.ll.payment.payment.service.refund.PaymentRefundService;
import com.ll.payment.payment.service.toss.TossPaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DepositPaymentServiceImpl implements DepositPaymentService {

    private final DepositService depositService;
    private final PaymentJpaRepository paymentJpaRepository;
    private final PaymentHistoryJpaRepository paymentHistoryJpaRepository;
    private final TossPaymentService tossPaymentService;
    private final PaymentRefundService paymentRefundService;

    @Override
    @Transactional
    public PaymentProcessResult depositPayment(PaymentRequest payment) {
        DepositResponse depositInfo = depositService.getDepositByUserCode(payment.buyerCode());

        int currentBalance = depositInfo.balance().intValue();
        int requestedAmount = payment.paidAmount();

        // 예치금이 충분한 경우 바로 예치금 결제
        if (currentBalance >= requestedAmount) {
            // 결제 요청 이력 저장
            PaymentHistoryEntity requestHistory = PaymentHistoryEntity.create(
                    null, // paymentId (아직 생성 전)
                    PaymentHistoryActionType.REQUEST,
                    PaymentStatus.PENDING,
                    "DEPOSIT", // pgName
                    null, // paymentKey
                    null, // transactionId
                    requestedAmount,
                    null, // failCode
                    null, // failMessage
                    null, // metadata
                    null, // approvedAt
                    null  // refundedAt
            );
            paymentHistoryJpaRepository.save(requestHistory);

            Payment depositPayment = completeDepositPayment(payment, requestedAmount);
            log.debug("예치금 결제 완료 - orderId: {}, amount: {}", payment.orderId(), requestedAmount);
            return new PaymentProcessResult(depositPayment, null);
        }

        // 예치금이 부족한 경우 : 토스 결제로 금액 충전 + 예치금으로 전체 결제
        int shortageAmount = requestedAmount - currentBalance;
        log.debug("예치금 부족 - 현재 잔액: {}, 요청 금액: {}, 부족 금액: {}", 
                currentBalance, requestedAmount, shortageAmount);
        
        Payment chargeTossPayment = null;
        try {
            // 토스 결제로 예치금 충전
            chargeTossPayment = tossPaymentService.chargeDepositWithToss(payment, shortageAmount);
            log.debug("토스 충전 완료 - paymentId: {}, amount: {}", 
                    chargeTossPayment.getId(), shortageAmount);
        } catch (Exception e) {
            log.error("토스 결제로 예치금 충전 실패 - orderId: {}, shortageAmount: {}, error: {}", 
                    payment.orderId(), shortageAmount, e.getMessage(), e);
            
            // 토스 결제는 성공했지만 예치금 충전 실패한 경우 토스 환불
            if (chargeTossPayment != null && chargeTossPayment.getPaymentStatus() == PaymentStatus.CHARGE) {
                try {
                    paymentRefundService.processTossRefundForCharge(chargeTossPayment, shortageAmount);
                    log.debug("예치금 충전 실패로 인한 토스 결제 환불 완료 - paymentId: {}", chargeTossPayment.getId());
                } catch (Exception refundException) {
                    log.error("토스 결제 환불 실패 - paymentId: {}, error: {}", 
                            chargeTossPayment.getId(), refundException.getMessage(), refundException);
                    // 환불도 실패한 경우는 수동 처리 필요
                }
            }
            throw new BaseException(PaymentErrorCode.TOSS_PAYMENT_CREATE_FAILED);
        }

        // 예치금 충전 후 예치금 결제
        Payment depositPayment = completeDepositPayment(payment, requestedAmount);
        log.debug("예치금 결제 완료 - orderId: {}, amount: {}", payment.orderId(), requestedAmount);

        return new PaymentProcessResult(depositPayment, chargeTossPayment);
    }

    @Override
    @Transactional
    public Payment completeDepositPayment(PaymentRequest payment, int amount) {
        // 1. 비관적 락으로 중복 결제 체크
        Optional<Payment> existingPayment = paymentJpaRepository
                .findByOrderIdAndPaymentStatusWithLock(
                        payment.orderId(),
                        PaymentStatus.COMPLETED
                );
        
        if (existingPayment.isPresent()) {
            log.warn("이미 결제 완료된 주문입니다. orderId: {}, paymentId: {}",
                    payment.orderId(), existingPayment.get().getId());
            return existingPayment.get();
        }
        
        // 2. 예치금 차감 (락 유지 중)
        depositService.withdrawDeposit(
                payment.buyerCode(),
                new DepositTransactionRequest((long) amount, createReferenceCode(payment.orderId()))
        );

        // 3. Payment 엔티티 생성 및 저장 (락 유지 중)
        Payment depositPayment = Payment.createDepositPayment(
                payment.orderId(),
                payment.buyerId(),
                amount,
                0L
        );
        paymentJpaRepository.save(depositPayment);

        // 4. 결제 성공 이력 저장 (락 유지 중)
        PaymentHistoryEntity paymentHistory = PaymentHistoryEntity.create(
                depositPayment.getId(),
                PaymentHistoryActionType.SUCCESS,
                PaymentStatus.COMPLETED,
                "DEPOSIT", // pgName
                null, // paymentKey (예치금 결제는 없음)
                null, // transactionId
                amount,
                null, // failCode
                null, // failMessage
                null, // metadata
                LocalDateTime.now(), // approvedAt
                null  // refundedAt
        );
        paymentHistoryJpaRepository.save(paymentHistory);

        return depositPayment;
    }

    private String createReferenceCode(Long orderId) {
        return "ORDER-" + orderId + "-" + System.currentTimeMillis();
    }
}

