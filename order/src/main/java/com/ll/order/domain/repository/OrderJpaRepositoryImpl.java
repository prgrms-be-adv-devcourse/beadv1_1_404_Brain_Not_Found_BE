package com.ll.order.domain.repository;

import com.ll.order.domain.model.entity.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class OrderJpaRepositoryImpl {

    private final OrderJpaRepository orderJpaRepository;

    public Order findByCode(String orderCode) {
        return Optional.ofNullable(orderJpaRepository.findByCode(orderCode))
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다: " + orderCode));
    }
}
