package com.ll.order.domain.repository;

import com.ll.order.domain.model.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderJpaRepository extends JpaRepository<Order, Long> {
    
    Order findByOrderCode(String orderCode);
}
