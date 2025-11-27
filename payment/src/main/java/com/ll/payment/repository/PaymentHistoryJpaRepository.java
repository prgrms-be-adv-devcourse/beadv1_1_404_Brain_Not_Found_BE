package com.ll.payment.repository;

import com.ll.payment.model.entity.history.PaymentHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentHistoryJpaRepository extends JpaRepository<PaymentHistoryEntity, Long> {
    List<PaymentHistoryEntity> findByPaymentIdOrderByStatusChangedAtDesc(Long paymentId);
    List<PaymentHistoryEntity> findByOrderIdOrderByStatusChangedAtDesc(Long orderId);
}

