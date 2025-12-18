package com.ll.order.domain.repository;

import com.ll.order.domain.model.entity.event.PaymentRefundEventOutbox;
import com.ll.order.domain.model.enums.order.OutboxStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PaymentRefundRequestEventOutboxRepository extends JpaRepository<PaymentRefundEventOutbox, Long> {
    
    @Query("""
            SELECT o FROM PaymentRefundEventOutbox o
            WHERE o.status = :status AND o.retryCount < :maxRetryCount
            """)
    List<PaymentRefundEventOutbox> findByStatusAndRetryCountLessThan(
            @Param("status") OutboxStatus status,
            @Param("maxRetryCount") Integer maxRetryCount
    );
}

