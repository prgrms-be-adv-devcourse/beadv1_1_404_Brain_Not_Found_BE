package com.ll.order.domain.repository;

import com.ll.order.domain.model.entity.OrderEventOutbox;
import com.ll.order.domain.model.entity.OrderEventOutbox.OutboxStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderEventOutboxRepository extends JpaRepository<OrderEventOutbox, Long> {
    
    @Query("SELECT o FROM OrderEventOutbox o WHERE o.status = :status AND o.retryCount < :maxRetryCount")
    List<OrderEventOutbox> findByStatusAndRetryCountLessThan(
            @Param("status") OutboxStatus status,
            @Param("maxRetryCount") Integer maxRetryCount
    );
}

