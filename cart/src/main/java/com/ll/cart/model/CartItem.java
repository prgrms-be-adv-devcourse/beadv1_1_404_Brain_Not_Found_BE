package com.ll.cart.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "cart_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "cart_item_code", nullable = false, unique = true)
    private String cartItemCode;

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

    public CartItem(Long productId,
                    String cartItemCode,
                    Integer quantity,
                    Integer totalPrice) {
        this.productId = productId;
        this.cartItemCode = cartItemCode;
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
}

