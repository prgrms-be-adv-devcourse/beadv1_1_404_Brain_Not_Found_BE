//package com.ll.order.domain.repository;
//
//import com.ll.order.domain.model.entity.event.SettlementRefundEventOutbox;
//import com.ll.order.domain.model.enums.order.OutboxStatus;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.data.repository.query.Param;
//
//import java.util.List;
//
//public interface RefundEventOutboxRepository extends JpaRepository<SettlementRefundEventOutbox, Long> {
//
//    @Query("""
//            SELECT o FROM SettlementRefundEventOutbox o
//            WHERE o.status = :status AND o.retryCount < :maxRetryCount
//            """)
//    List<SettlementRefundEventOutbox> findByStatusAndRetryCountLessThan(
//            @Param("status") OutboxStatus status,
//            @Param("maxRetryCount") Integer maxRetryCount
//    );
//}
//
