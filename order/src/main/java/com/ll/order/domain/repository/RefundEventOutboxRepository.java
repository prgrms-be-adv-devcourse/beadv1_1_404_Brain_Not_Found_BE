package com.ll.order.domain.repository;

import com.ll.order.domain.model.entity.event.RefundEventOutbox;
import com.ll.order.domain.model.enums.order.OutboxStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RefundEventOutboxRepository extends JpaRepository<RefundEventOutbox, Long> {
    
    @Query("""
            SELECT o FROM RefundEventOutbox o
            WHERE o.status = :status AND o.retryCount < :maxRetryCount
            """)
    List<RefundEventOutbox> findByStatusAndRetryCountLessThan(
            @Param("status") OutboxStatus status,
            @Param("maxRetryCount") Integer maxRetryCount
    );
}

