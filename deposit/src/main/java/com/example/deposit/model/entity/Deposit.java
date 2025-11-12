package com.example.deposit.model.entity;

import com.example.core.model.persistence.BaseEntity;
import com.example.deposit.model.enums.DepositStatus;
import com.example.deposit.model.exception.InsufficientDepositBalanceException;
import com.example.deposit.model.exception.InvalidDepositAmountException;
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
    private Long userId;

    @Column( nullable = false )
    private Long balance;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private DepositStatus depositStatus;

    @OneToMany(
            mappedBy = "deposit",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private List<DepositHistory> histories = new ArrayList<>();

    @Builder
    public Deposit(Long userId, Long balance, DepositStatus depositStatus) {
        this.userId = userId;
        this.balance = balance;
        this.depositStatus = depositStatus;
    }

    public static Deposit createInitialDeposit(Long userId) {
        return Deposit.builder()
                .userId(userId)
                .balance(0L)
                .depositStatus(DepositStatus.ACTIVE)
                .build();
    }

    public void addHistory(DepositHistory history) {
        histories.add(history);
        history.setDeposit(this);
    }

    public void charge(Long amount) {
        if (amount <= 0) {
            throw new InvalidDepositAmountException("금액은 0보다 커야 합니다.");
        }
        if (this.depositStatus != DepositStatus.ACTIVE) {
            throw new InvalidDepositStatusTransitionException("비활성 또는 종료된 입금 계좌에는 충전할 수 없습니다.");
        }
        this.balance += amount;
    }

    public void withdraw(Long amount) {
        if (amount < 0) {
            throw new InvalidDepositAmountException("금액은 0보다 커야 합니다.");
        }
        if (this.balance < amount) {
            throw new InsufficientDepositBalanceException("잔액이 부족합니다.");
        }
        this.balance -= amount;
    }

    public void setInActive() {
        if (this.depositStatus == DepositStatus.INACTIVE) {
            throw new InvalidDepositStatusTransitionException("이미 비활성 상태인 입금 계좌입니다.");
        }
        this.depositStatus = DepositStatus.INACTIVE;
    }

    public void setActive() {
        if (this.depositStatus == DepositStatus.ACTIVE) {
            throw new InvalidDepositStatusTransitionException("이미 활성 상태인 입금 계좌입니다.");
        }
        this.depositStatus = DepositStatus.ACTIVE;
    }

    public void setClosed() {
        if (this.depositStatus == DepositStatus.CLOSED) {
            throw new InvalidDepositStatusTransitionException("이미 종료 상태인 입금 계좌입니다.");
        }
        this.depositStatus = DepositStatus.CLOSED;
    }
}
