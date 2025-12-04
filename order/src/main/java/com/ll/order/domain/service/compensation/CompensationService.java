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
        try {
            TransactionTracing tracing = transactionTracingRepository.findByOrderCode(orderCode)
                    .orElse(null);

            if (tracing != null) {
                // 보상 시작 상태로 변경 (아직 시작하지 않았다면)
                if (tracing.getCompensationStatus() == CompensationStatus.NONE) {
                    tracing.startCompensation();
                }
                // 보상 실패 상태로 변경
                tracing.markCompensationFailed(errorMessage);

                log.debug("보상 로직 실패 상태 저장 완료 - orderCode: {}, retryCount: {}",
                        orderCode, tracing.getCompensationRetryCount());
            } else {
                log.debug("TransactionTracing을 찾을 수 없습니다. orderCode: {}", orderCode);
            }
        } catch (Exception e) {
            log.error("보상 로직 실패 상태 저장 실패 - orderCode: {}, error: {}",
                    orderCode, e.getMessage(), e);
        }
    }
}
