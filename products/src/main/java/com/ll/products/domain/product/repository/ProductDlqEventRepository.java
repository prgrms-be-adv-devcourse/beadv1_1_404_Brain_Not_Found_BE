package com.ll.products.domain.product.repository;

import com.ll.products.domain.product.model.entity.ProductDlqEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductDlqEventRepository extends JpaRepository<ProductDlqEvent, Long> {
}