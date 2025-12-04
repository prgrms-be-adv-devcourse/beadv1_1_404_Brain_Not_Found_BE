package com.ll.payment.deposit.model.entity;

import com.ll.core.model.persistence.BaseEntity;
import com.ll.payment.deposit.model.enums.DepositHistoryType;
import com.ll.payment.deposit.model.enums.DepositStatus;
import com.ll.payment.deposit.model.exception.DepositAlreadyExistsException;
import com.ll.payment.deposit.model.exception.DepositBalanceNotEmptyException;
import com.ll.payment.deposit.model.exception.InsufficientDepositBalanceException;
import com.ll.payment.deposit.model.exception.InvalidDepositStatusTransitionException;
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

    public DepositHistory charge(Long amount, String referenceCode) {
        return increaseBalance(amount, referenceCode, DepositHistoryType.CHARGE);
    }
    
    public DepositHistory refund(Long amount, String referenceCode) {
        return increaseBalance(amount, referenceCode, DepositHistoryType.REFUND);
    }

    public DepositHistory withdraw(Long amount, String referenceCode) {
        return decreaseBalance(amount, referenceCode, DepositHistoryType.WITHDRAW);
    }
    
    public DepositHistory payment(Long amount, String referenceCode) {
        return decreaseBalance(amount, referenceCode, DepositHistoryType.PAYMENT);
    }

    private DepositHistory increaseBalance(Long amount, String referenceCode, DepositHistoryType type) {
        Long before = this.balance;
        validateActive();
        this.balance += amount;
        return DepositHistory.create(this.getId(), amount, before, this.balance, referenceCode, type);
    }

    private DepositHistory decreaseBalance(Long amount, String referenceCode, DepositHistoryType type) {
        Long before = this.balance;
        validateActive();
        validateSufficientBalance(amount);
        this.balance -= amount;
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

    private void validateSufficientBalance(Long amount) {
        if (amount == null || this.balance <= amount) {
            throw new InsufficientDepositBalanceException();
        }
    }
}
