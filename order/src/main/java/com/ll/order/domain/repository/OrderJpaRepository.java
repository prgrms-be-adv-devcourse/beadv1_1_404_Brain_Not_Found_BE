package com.ll.order.domain.repository;

import com.ll.order.domain.model.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderJpaRepository extends JpaRepository<Order, Long> {

    Order findByOrderCode(String orderCode);

    Page<Order> findByBuyerId(Long buyerId, Pageable pageable);

    @Query("""
               SELECT DISTINCT o FROM Order o 
               JOIN o.orderItems oi
               WHERE o.buyerId = :buyerId
               AND oi.productName LIKE %:keyword%
            """)
    Page<Order> findByBuyerIdAndProductNameContaining(@Param("sellerId") Long buyerId,
                                                      @Param("keyword") String keyword,
                                                      Pageable pageable);
}
