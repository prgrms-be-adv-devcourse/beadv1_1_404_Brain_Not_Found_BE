package com.ll.payment.payment.scheduler;

import com.ll.payment.payment.service.event.PaymentRefundNotificationOutboxService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "payment.outbox.scheduler.enabled", havingValue = "true", matchIfMissing = false)
public class PaymentRefundNotificationOutboxScheduler {

    private final PaymentRefundNotificationOutboxService paymentRefundNotificationOutboxService;

    // PENDING 상태의 알림을 주기적으로 전송
    @Scheduled(cron = "${payment.outbox.scheduler.cron:0 */5 * * * ?}")
    public void publishPendingNotifications() {
        try {
            long publishableCount = paymentRefundNotificationOutboxService.countPublishableNotifications();
            
            if (publishableCount == 0) {
                log.debug("전송할 PENDING 상태의 환불 알림이 없습니다.");
            } else {
                log.debug("스케줄러 실행 - 전송 대상 알림: {}개", publishableCount);
                int successCount = paymentRefundNotificationOutboxService.publishPendingNotifications();
                log.debug("스케줄러 완료 - 전송 성공: {}개", successCount);
            }

            // FAILED 상태의 알림도 재전송 시도
            long republishableCount = paymentRefundNotificationOutboxService.countRepublishableNotifications();
            
            if (republishableCount == 0) {
                log.debug("재전송할 FAILED 상태의 환불 알림이 없습니다.");
            } else {
                log.debug("스케줄러 실행 - 재전송 대상 알림: {}개", republishableCount);
                int successCount = paymentRefundNotificationOutboxService.republishFailedNotifications();
                log.debug("스케줄러 완료 - 재전송 성공: {}개", successCount);
            }
        } catch (Exception e) {
            log.error("스케줄러 실행 중 오류 발생", e);
        }
    }
}

