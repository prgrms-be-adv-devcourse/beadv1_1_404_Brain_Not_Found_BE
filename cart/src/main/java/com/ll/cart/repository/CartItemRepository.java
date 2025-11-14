package com.ll.cart.repository;

import com.ll.cart.model.Cart;
import com.ll.cart.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    Optional<CartItem> findByCartAndProductId(Cart cart, Long productId);

    Optional<CartItem> findByCode(String code);
}

