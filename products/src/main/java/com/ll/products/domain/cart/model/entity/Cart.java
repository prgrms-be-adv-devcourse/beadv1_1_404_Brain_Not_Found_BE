package com.ll.products.domain.cart.model.entity;

import com.ll.core.model.persistence.BaseEntity;
import com.ll.products.domain.cart.model.enums.CartStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "carts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Cart extends BaseEntity {

    @Column(nullable = false, unique = true)
    private Long userId;

    @Enumerated(EnumType.STRING)
    private CartStatus status;

    private Integer totalPrice = 0;

    public Cart(Long userId, CartStatus status) {
        this.userId = userId;
        this.status = status;
    }

    public void increaseTotalPrice(int amount) {
        totalPrice += amount;
    }

    public void decreaseTotalPrice(int amount) {
        totalPrice -= amount;
        if (totalPrice < 0) {
            totalPrice = 0;
        }
    }
}
