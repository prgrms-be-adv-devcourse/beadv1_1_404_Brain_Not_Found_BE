package com.example.deposit.repository;

import com.example.deposit.model.entity.Deposit;
import com.example.deposit.model.entity.DepositHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface DepositHistoryRepository extends JpaRepository<DepositHistory, Long> {

    boolean existsByReferenceCode(String referenceCode);

    @Query("""
    SELECT dh
    FROM DepositHistory dh
    WHERE dh.deposit = :deposit
      AND (:fromDate IS NULL OR dh.createdAt >= :fromDate)
      AND (:toDate IS NULL OR dh.createdAt < :toDate)
    """)
    Page<DepositHistory> findAllByDepositAndCreatedAtBetween(
            @Param("deposit") Deposit deposit,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            Pageable pageable
    );

}
