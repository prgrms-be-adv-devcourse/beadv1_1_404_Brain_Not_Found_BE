package com.ll.payment.deposit.repository;

import com.ll.payment.deposit.model.entity.Deposit;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DepositRepository extends JpaRepository<Deposit, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Deposit> findByUserCode(String userCode);

    @Query("SELECT d FROM Deposit d WHERE d.userCode = :userCode")
    Optional<Deposit> findByUserCodeWithOutLock(String userCode);
}
