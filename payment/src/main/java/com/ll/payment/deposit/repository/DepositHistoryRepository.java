package com.ll.payment.deposit.repository;

import com.ll.payment.deposit.model.entity.DepositHistory;
import com.ll.payment.deposit.repository.custom.CustomDepositHistoryRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DepositHistoryRepository extends JpaRepository<DepositHistory, Long>, CustomDepositHistoryRepository {
    boolean existsByReferenceCode(String referenceCode);
}
