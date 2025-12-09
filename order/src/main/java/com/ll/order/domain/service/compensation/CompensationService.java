package com.ll.order.domain.service.compensation;

import com.ll.order.domain.model.entity.TransactionTracing;
import com.ll.order.domain.model.enums.transaction.CompensationStatus;
import com.ll.order.domain.repository.TransactionTracingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompensationService {

    private final TransactionTracingRepository transactionTracingRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void createTransactionTracing(String orderCode) {
        try {
            TransactionTracing tracing = TransactionTracing.builder()
                    .orderCode(orderCode)
                    .build();
            transactionTracingRepository.save(tracing);
            log.debug("TransactionTracing 생성 완료 - orderCode: {}", orderCode);
        } catch (Exception e) {
            log.error("TransactionTracing 생성 실패 - orderCode: {}, error: {}",
                    orderCode, e.getMessage(), e);
            // TransactionTracing 생성 실패는 치명적이지 않으므로 예외를 던지지 않음
            // 보상 로직이 동작하지 않을 수 있지만, 주문 생성은 계속 진행
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markCompensationStarted(String orderCode) {
        try {
            TransactionTracing tracing = transactionTracingRepository.findByOrderCode(orderCode)
                    .orElse(null);

            if (tracing != null) {
                tracing.startCompensation();
                log.debug("보상 시작 상태 저장 완료 - orderCode: {}", orderCode);
            } else {
                log.debug("TransactionTracing을 찾을 수 없습니다. orderCode: {}", orderCode);
            }
        } catch (Exception e) {
            log.error("보상 시작 상태 저장 실패 - orderCode: {}, error: {}",
                    orderCode, e.getMessage(), e);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markCompensationCompleted(String orderCode) {
        try {
            TransactionTracing tracing = transactionTracingRepository.findByOrderCode(orderCode)
                    .orElse(null);

            if (tracing != null) {
                tracing.markCompensationCompleted();
                log.debug("보상 완료 상태 저장 완료 - orderCode: {}", orderCode);
            } else {
                log.debug("TransactionTracing을 찾을 수 없습니다. orderCode: {}", orderCode);
            }
        } catch (Exception e) {
            log.error("보상 완료 상태 저장 실패 - orderCode: {}, error: {}",
                    orderCode, e.getMessage(), e);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markCompensationFailed(String orderCode, String errorMessage) {
        log.info("=== CompensationService.markCompensationFailed 호출됨 - orderCode: {}, errorMessage: {} ===", orderCode, errorMessage);
        try {
            log.info("TransactionTracing 조회 시작 - orderCode: {}", orderCode);
            TransactionTracing tracing = transactionTracingRepository.findByOrderCode(orderCode)
                    .orElse(null);
            log.info("TransactionTracing 조회 결과 - tracing: {}", tracing != null ? "존재함" : "null");

            if (tracing != null) {
                log.info("보상 상태 변경 시작 - 현재 상태: {}", tracing.getCompensationStatus());
                // 보상 시작 상태로 변경 (아직 시작하지 않았다면)
                if (tracing.getCompensationStatus() == CompensationStatus.NONE) {
                    tracing.startCompensation();
                    log.info("보상 시작 상태로 변경 완료");
                }
                // 보상 실패 상태로 변경
                tracing.markCompensationFailed(errorMessage);
                log.info("보상 실패 상태로 변경 완료 - retryCount: {}", tracing.getCompensationRetryCount());

                log.info("보상 로직 실패 상태 저장 완료 - orderCode: {}, retryCount: {}",
                        orderCode, tracing.getCompensationRetryCount());
            } else {
                log.warn("TransactionTracing을 찾을 수 없습니다. orderCode: {}", orderCode);
            }
        } catch (Exception e) {
            log.error("보상 로직 실패 상태 저장 실패 - orderCode: {}, error: {}",
                    orderCode, e.getMessage(), e);
        }
        log.info("=== CompensationService.markCompensationFailed 종료 ===");
    }
}
