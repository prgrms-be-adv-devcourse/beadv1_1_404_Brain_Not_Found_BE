package com.ll.cart.model.entity;

import com.example.core.model.persistence.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "cart_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CartItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    private Cart cart;

    private Long productId;

    private Integer quantity;

    private Integer price;

    private Integer totalPrice;

    private CartItem(Long productId,
                     Integer quantity,
                     Integer price) {
        this.productId = productId;
        this.quantity = quantity;
        this.price = price;
        calculateTotalPrice();
    }

    void assignCart(Cart cart) {
        this.cart = cart;
    }

    private void calculateTotalPrice() {
        if (this.price != null && this.quantity != null) {
            this.totalPrice = this.price * this.quantity;
        }
    }

    public void changeQuantity(int quantity, int price) {
        this.quantity = quantity;
        this.price = price;
        calculateTotalPrice();
    }

    public static CartItem create(Cart cart,
                                  Long productId,
                                  Integer quantity,
                                  Integer price) {
        CartItem cartItem = new CartItem(productId, quantity, price);
        cartItem.assignCart(cart);
        return cartItem;
    }
}