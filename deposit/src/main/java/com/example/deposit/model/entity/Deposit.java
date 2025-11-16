package com.example.deposit.model.entity;

import com.example.core.model.persistence.BaseEntity;
import com.example.deposit.model.enums.DepositStatus;
import com.example.deposit.model.exception.DepositAlreadyExistsException;
import com.example.deposit.model.exception.DepositBalanceNotEmptyException;
import com.example.deposit.model.exception.InsufficientDepositBalanceException;
import com.example.deposit.model.exception.InvalidDepositStatusTransitionException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.*;

@Entity
@Getter
@Table(name = "deposits")
@NoArgsConstructor( access = AccessLevel.PROTECTED )
public class Deposit extends BaseEntity {

    @Column( unique = true, nullable = false )
    private String userCode;

    @Column( nullable = false )
    private Long balance;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private DepositStatus depositStatus;

    @Builder
    public Deposit(String userCode, Long balance, DepositStatus depositStatus) {
        this.userCode = userCode;
        this.balance = balance;
        this.depositStatus = depositStatus;
    }

    public static Deposit createInitialDeposit(String userCode) {
        return Deposit.builder()
                .userCode(userCode)
                .balance(0L)
                .depositStatus(DepositStatus.ACTIVE)
                .build();
    }

    public Deposit charge(Long amount) {
        if (this.depositStatus != DepositStatus.ACTIVE) {
            throw new InvalidDepositStatusTransitionException("비활성 또는 종료된 입금 계좌에는 충전할 수 없습니다.");
        }
        this.balance += amount;
        return  this;
    }

    public Deposit withdraw(Long amount) {
        if (this.depositStatus != DepositStatus.ACTIVE) {
            throw new InvalidDepositStatusTransitionException("비활성 계좌는 출금할 수 없습니다.");
        }
        if (this.balance < amount) {
            throw new InsufficientDepositBalanceException("잔액이 부족합니다.");
        }
        this.balance -= amount;
        return this;
    }

    public Deposit setClosed() {
        if (this.depositStatus == DepositStatus.CLOSED) {
            throw new InvalidDepositStatusTransitionException();
        }
        if (this.balance > 0) {
            throw new DepositBalanceNotEmptyException();
        }
        this.depositStatus = DepositStatus.CLOSED;
        return this;
    }

    public Deposit setActive() {
        if (this.depositStatus == DepositStatus.ACTIVE) {
            throw new DepositAlreadyExistsException();
        }
        System.out.printf("Closed deposit found for user %s. Reactivating...\n", userCode);
        this.depositStatus = DepositStatus.ACTIVE;
        return this;
    }
}
