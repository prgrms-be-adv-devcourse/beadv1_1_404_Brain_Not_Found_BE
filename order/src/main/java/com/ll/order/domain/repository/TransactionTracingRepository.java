package com.ll.order.domain.repository;

import com.ll.order.domain.model.entity.TransactionTracing;
import com.ll.order.domain.model.entity.TransactionTracing.CompensationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TransactionTracingRepository extends JpaRepository<TransactionTracing, Long> {
    
    Optional<TransactionTracing> findByOrderCode(String orderCode);
    
    @Query("""
        SELECT t FROM TransactionTracing t 
        WHERE t.compensationStatus = :compensationStatus 
        AND t.compensationRetryCount < :maxRetryCount 
        AND t.status = 'COMPENSATING'
        """)
    List<TransactionTracing> findFailedCompensationsForRetry(
            @Param("compensationStatus") CompensationStatus compensationStatus,
            @Param("maxRetryCount") Integer maxRetryCount
    );
}

