package com.ll.payment.repository;

import com.ll.payment.model.entity.Payment;
import com.ll.payment.model.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentJpaRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByPaymentCode(String paymentCode);
    Optional<Payment> findByOrderIdAndPaymentStatus(Long orderId, PaymentStatus paymentStatus);
    Optional<Payment> findByIdAndPaymentStatus(Long id, PaymentStatus paymentStatus);
    Optional<Payment> findByPaymentCodeAndPaymentStatus(String paymentCode, PaymentStatus paymentStatus);
}
