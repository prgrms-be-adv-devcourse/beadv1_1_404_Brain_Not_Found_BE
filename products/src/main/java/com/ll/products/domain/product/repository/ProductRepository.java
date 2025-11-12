package com.ll.products.domain.product.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.ll.products.domain.product.model.entity.Product;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long>, ProductRepositoryCustom {

    Optional<Product> findByCodeAndIsDeletedFalse(String code);

    boolean existsByCodeAndIsDeletedFalse(String code);
}