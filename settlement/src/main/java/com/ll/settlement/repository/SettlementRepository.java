package com.ll.settlement.repository;

import com.ll.settlement.model.entity.Settlement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SettlementRepository extends JpaRepository<Settlement, Long> {
    Optional<Settlement> findByOrderItemCode(String orderItemCode);
}
