package com.ll.payment.payment.repository;

import com.ll.payment.payment.model.entity.PaymentHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentHistoryJpaRepository extends JpaRepository<PaymentHistoryEntity, Long> {
}

