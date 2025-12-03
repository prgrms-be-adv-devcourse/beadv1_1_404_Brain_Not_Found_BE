package com.ll.order.domain.scheduler;

import com.ll.order.domain.service.OrderEventOutboxService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "order.outbox.scheduler.enabled", havingValue = "true", matchIfMissing = false)
public class OrderEventOutboxScheduler {

    private final OrderEventOutboxService orderEventOutboxService;

    // PENDING 상태의 이벤트를 주기적으로 발행
    @Scheduled(cron = "${order.outbox.scheduler.cron:0 */5 * * * ?}")
    public void publishPendingEvents() {
        try {
            long publishableCount = orderEventOutboxService.countPublishableEvents();
            
            if (publishableCount == 0) {
                log.debug("발행할 PENDING 상태의 이벤트가 없습니다.");
            } else {
                log.debug("스케줄러 실행 - 발행 대상 이벤트: {}개", publishableCount);
                int successCount = orderEventOutboxService.publishPendingEvents();
                log.debug("스케줄러 완료 - 발행 성공: {}개", successCount);
            }

            // FAILED 상태의 이벤트도 재발행 시도
            long republishableCount = orderEventOutboxService.countRepublishableEvents();
            
            if (republishableCount == 0) {
                log.debug("재발행할 FAILED 상태의 이벤트가 없습니다.");
            } else {
                log.debug("스케줄러 실행 - 재발행 대상 이벤트: {}개", republishableCount);
                int successCount = orderEventOutboxService.republishFailedEvents();
                log.debug("스케줄러 완료 - 재발행 성공: {}개", successCount);
            }
        } catch (Exception e) {
            log.error("스케줄러 실행 중 오류 발생", e);
        }
    }
}
