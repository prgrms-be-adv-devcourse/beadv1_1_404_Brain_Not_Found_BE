package com.ll.order.domain.scheduler;

import com.ll.order.domain.service.CompensationRetryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "order.compensation.scheduler.enabled", havingValue = "true", matchIfMissing = false)
public class CompensationRetryScheduler {

    private final CompensationRetryService compensationRetryService;

    // 실패한 보상 로직을 주기적으로 재시도
    @Scheduled(cron = "${order.compensation.scheduler.cron:0 */10 * * * ?}")
    public void retryFailedCompensations() {
        try {
            long retryableCount = compensationRetryService.countRetryableCompensations();
            
            if (retryableCount == 0) {
                log.debug("재시도할 보상 로직이 없습니다.");
                return;
            }

            log.debug("스케줄러 실행 - 재시도 대상 보상 로직: {}개", retryableCount);
            
            int successCount = compensationRetryService.retryFailedCompensations();
            
            log.debug("스케줄러 완료 - 재시도 성공: {}개", successCount);
        } catch (Exception e) {
            log.error("스케줄러 실행 중 오류 발생", e);
        }
    }
}

