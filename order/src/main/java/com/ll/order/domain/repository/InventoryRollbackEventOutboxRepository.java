package com.ll.order.domain.repository;

import com.ll.order.domain.model.entity.event.InventoryRollbackEventOutbox;
import com.ll.order.domain.model.entity.event.InventoryRollbackEventOutbox.CompensationOutboxStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface InventoryRollbackEventOutboxRepository extends JpaRepository<InventoryRollbackEventOutbox, Long> {
    
    @Query("""
            SELECT o FROM InventoryRollbackEventOutbox o 
            WHERE o.status = :status AND o.retryCount < :maxRetryCount
            """)
    List<InventoryRollbackEventOutbox> findByStatusAndRetryCountLessThan(
            @Param("status") CompensationOutboxStatus status,
            @Param("maxRetryCount") Integer maxRetryCount
    );
}

