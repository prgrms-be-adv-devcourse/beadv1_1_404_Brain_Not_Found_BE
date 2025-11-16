package com.ll.cart.repository;

import com.ll.cart.model.entity.Cart;
import com.ll.cart.model.entity.CartItem;
import com.ll.cart.model.enums.CartStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    Optional<CartItem> findByCartAndProductId(Cart cart, Long productId);

    Optional<CartItem> findByCode(String code);

    @Query("SELECT ci FROM CartItem ci JOIN ci.cart c WHERE ci.code = :code AND c.status = :status")
    Optional<CartItem> findByCodeAndCartStatus(@Param("code") String code, @Param("status") CartStatus status);

    List<CartItem> findByCart(Cart cart);
}

