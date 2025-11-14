package com.ll.cart.model;

import com.example.core.model.persistence.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "cart_items")
@AttributeOverride(name = "code", column = @Column(name = "cart_item_code", nullable = false, unique = true, updatable = false))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CartItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "total_price", nullable = false)
    private Integer totalPrice;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

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