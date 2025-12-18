package com.ll.payment.payment.repository;

import com.ll.payment.payment.model.entity.event.PaymentRefundNotificationOutbox;
import com.ll.payment.payment.model.enums.PaymentOutboxStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PaymentRefundNotificationOutboxRepository extends JpaRepository<PaymentRefundNotificationOutbox, Long> {
    
    @Query("""
            SELECT o FROM PaymentRefundNotificationOutbox o
            WHERE o.status = :status AND o.retryCount < :maxRetryCount
            """)
    List<PaymentRefundNotificationOutbox> findByStatusAndRetryCountLessThan(
            @Param("status") PaymentOutboxStatus status,
            @Param("maxRetryCount") Integer maxRetryCount
    );
}

