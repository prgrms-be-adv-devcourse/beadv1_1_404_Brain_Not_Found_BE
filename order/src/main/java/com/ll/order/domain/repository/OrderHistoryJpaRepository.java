package com.ll.order.domain.repository;

import com.ll.order.domain.model.entity.history.OrderHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderHistoryJpaRepository extends JpaRepository<OrderHistoryEntity, Long> {
}

