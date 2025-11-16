package com.ll.cart.model.entity;

import com.example.core.model.persistence.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "cart_items")
@AttributeOverride(name = "code", column = @Column(name = "cart_item_code", nullable = false, unique = true, updatable = false))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CartItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    private Cart cart;

    private Long productId;

    private Integer quantity;

    private Integer totalPrice;

    private CartItem(Long productId,
                     Integer quantity,
                     Integer totalPrice) {
        this.productId = productId;
        this.quantity = quantity;
        this.totalPrice = totalPrice;
    }

    void assignCart(Cart cart) {
        this.cart = cart;
    }

    public void changeQuantity(int quantity, int totalPrice) {
        this.quantity = quantity;
        this.totalPrice = totalPrice;
    }

    public static CartItem create(Cart cart,
                                  Long productId,
                                  Integer quantity,
                                  Integer totalPrice) {
        CartItem cartItem = new CartItem(productId, quantity, totalPrice);
        cartItem.assignCart(cart);
        return cartItem;
    }
}