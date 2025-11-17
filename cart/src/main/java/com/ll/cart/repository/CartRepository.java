package com.ll.cart.repository;

import com.ll.cart.model.entity.Cart;
import com.ll.cart.model.enums.CartStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    Optional<Cart> findByUserIdAndStatus(Long userId, CartStatus status);

    Optional<Cart> findByCode(String code);

    Optional<Cart> findByCodeAndStatus(String code, CartStatus status);
}
