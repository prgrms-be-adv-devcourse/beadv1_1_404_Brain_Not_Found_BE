package com.example.deposit.repository;

import com.example.deposit.model.entity.Deposit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DepositRepository extends JpaRepository<Deposit, Long> {
    boolean existsByUserId(Long userId);
    Optional<Deposit> findByCode(String code);
    Optional<Deposit> findByUserId(Long userId);
}
