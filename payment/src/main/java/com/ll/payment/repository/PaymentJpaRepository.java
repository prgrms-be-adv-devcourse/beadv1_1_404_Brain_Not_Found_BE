package com.ll.payment.repository;

import com.ll.payment.model.entity.Payment;
import com.ll.payment.model.enums.PaymentStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentJpaRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByPaymentCode(String paymentCode);
    Optional<Payment> findByOrderIdAndPaymentStatus(Long orderId, PaymentStatus paymentStatus);

    @Query("SELECT p FROM Payment p WHERE p.orderId = :orderId AND p.paymentStatus = :status")
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Payment> findByOrderIdAndPaymentStatusWithLock(
            @Param("orderId") Long orderId, 
            @Param("status") PaymentStatus status
    );
}
