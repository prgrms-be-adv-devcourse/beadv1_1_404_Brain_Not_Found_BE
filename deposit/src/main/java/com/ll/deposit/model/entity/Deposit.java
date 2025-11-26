package com.ll.deposit.model.entity;

import com.ll.core.model.persistence.BaseEntity;
import com.ll.deposit.model.enums.DepositHistoryType;
import com.ll.deposit.model.enums.DepositStatus;
import com.ll.deposit.model.exception.*;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

    public void charge(Long amount) {
        validateActive();
        this.balance += amount;
    }

    public void withdraw(Long amount) {
        validateActive();
        if (this.balance < amount) {
            throw new InsufficientDepositBalanceException();
        }
        this.balance -= amount;
    }

    public DepositHistory applyTransaction(Long amount, String referenceCode, DepositHistoryType type) {
        Long before = this.balance;

        switch (type) {
            case CHARGE -> this.charge(amount);
            case WITHDRAW, PAYMENT -> this.withdraw(amount);
            default -> throw new InvalidDepositHistoryTypeException();
        }

        return DepositHistory.create(this.getId(), amount, before, this.balance, referenceCode, type);
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
        this.depositStatus = DepositStatus.ACTIVE;
        return this;
    }

    private void validateActive() {
        if (this.depositStatus != DepositStatus.ACTIVE) {
            throw new InvalidDepositStatusTransitionException();
        }
    }
}
