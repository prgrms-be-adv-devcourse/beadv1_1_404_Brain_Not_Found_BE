package com.ll.products.domain.product.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.ll.products.domain.product.model.entity.Product;
import com.ll.products.domain.product.model.entity.ProductStatus;

public interface ProductRepositoryCustom {

    Page<Product> searchProducts(
            String sellerCode,
            Long categoryId,
            ProductStatus status,
            String name,
            Pageable pageable
    );
}
