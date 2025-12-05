package com.ll.products.domain.product.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.ll.products.domain.product.model.entity.Product;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long>, ProductRepositoryCustom {

    @Query("SELECT DISTINCT p FROM Product p " +
           "LEFT JOIN FETCH p.images " +
           "LEFT JOIN FETCH p.category " +
           "WHERE p.code = :code AND p.isDeleted = false")
    Optional<Product> findByCodeAndIsDeletedFalse(@Param("code") String code);

    Optional<Product> findByIdAndIsDeleted(Long id, Boolean isDeleted);
    
    @Query("SELECT DISTINCT p FROM Product p " +
           "LEFT JOIN FETCH p.images " +
           "LEFT JOIN FETCH p.category " +
           "WHERE p.isDeleted = false")
    List<Product> findAllByIsDeletedFalse();

    /**
     * 비관적 락을 사용하여 상품 조회 (재고 수정 시 동시성 제어)
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Product p WHERE p.code = :code AND p.isDeleted = false")
    Optional<Product> findByCodeWithLock(@Param("code") String code);
}