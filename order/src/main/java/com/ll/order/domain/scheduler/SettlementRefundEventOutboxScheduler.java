//package com.ll.order.domain.scheduler;
//
//import com.ll.order.domain.service.event.RefundEventOutboxService;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//
//@Slf4j
//@Component
//@RequiredArgsConstructor
//@ConditionalOnProperty(name = "order.outbox.scheduler.enabled", havingValue = "true", matchIfMissing = false)
//public class SettlementRefundEventOutboxScheduler {
//
//    private final RefundEventOutboxService refundEventOutboxService;
//
//    @Scheduled(cron = "${order.outbox.scheduler.cron:0 */5 * * * ?}")
//    public void publishPendingEvents() {
//        try {
//            long publishableCount = refundEventOutboxService.countPublishableEvents();
//
//            if (publishableCount == 0) {
//                log.debug("[환불 이벤트(정산)] 발행할 PENDING 상태의 환불 이벤트가 없습니다.");
//            } else {
//                log.debug("[환불 이벤트(정산)] 스케줄러 실행 - 발행 대상 환불 이벤트: {}개", publishableCount);
//                int successCount = refundEventOutboxService.publishPendingEvents();
//                log.debug("[환불 이벤트(정산)] 스케줄러 완료 - 발행 성공: {}개", successCount);
//            }
//
//            long republishableCount = refundEventOutboxService.countRepublishableEvents();
//
//            if (republishableCount == 0) {
//                log.debug("[환불 이벤트(정산)] 재발행할 FAILED 상태의 환불 이벤트가 없습니다.");
//            } else {
//                log.debug("[환불 이벤트(정산)] 스케줄러 실행 - 재발행 대상 환불 이벤트: {}개", republishableCount);
//                int successCount = refundEventOutboxService.republishFailedEvents();
//                log.debug("[환불 이벤트(정산)] 스케줄러 완료 - 재발행 성공: {}개", successCount);
//            }
//        } catch (Exception e) {
//            log.error("[환불 이벤트(정산)] 스케줄러 실행 중 오류 발생", e);
//        }
//    }
//}
//
