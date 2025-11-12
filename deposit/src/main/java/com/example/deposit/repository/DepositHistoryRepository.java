package com.example.deposit.repository;

import com.example.deposit.model.entity.DepositHistory;
import com.example.deposit.repository.custom.DepositHistoryRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface DepositHistoryRepository extends JpaRepository<DepositHistory, Long>, DepositHistoryRepositoryCustom {
    boolean existsByReferenceCode(String referenceCode);
}
