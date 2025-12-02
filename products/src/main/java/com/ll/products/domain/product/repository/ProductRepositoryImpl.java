package com.ll.products.domain.product.repository;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import com.ll.products.domain.product.model.entity.Product;
import com.ll.products.domain.product.model.entity.ProductStatus;

import java.util.ArrayList;
import java.util.List;

import static com.ll.products.domain.product.model.entity.QProduct.product;

@Repository
@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Product> searchProducts(
            String sellerCode,
            Long categoryId,
            ProductStatus status,
            String name,
            Pageable pageable
    ) {
        List<Product> content = queryFactory
                .selectFrom(product)
                .where(
                        isDeletedFalse(),
                        sellerCodeEq(sellerCode),
                        categoryIdEq(categoryId),
                        statusEq(status),
                        nameContains(name)
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(getOrderSpecifiers(pageable))
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(product.count())
                .from(product)
                .where(
                        isDeletedFalse(),
                        sellerCodeEq(sellerCode),
                        categoryIdEq(categoryId),
                        statusEq(status),
                        nameContains(name)
                );

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    private BooleanExpression isDeletedFalse() {
        // 삭제된 상품은 어떤 경우에도 조회되지 않아야 함 (관리자 포함)
        return product.isDeleted.eq(false);
    }

    private BooleanExpression sellerCodeEq(String sellerCode) {
        return sellerCode != null && !sellerCode.isBlank() ? product.sellerCode.eq(sellerCode) : null;
    }

    private BooleanExpression categoryIdEq(Long categoryId) {
        return categoryId != null ? product.category.id.eq(categoryId) : null;
    }

    private BooleanExpression statusEq(ProductStatus status) {
        return status != null ? product.status.eq(status) : null;
    }

    private BooleanExpression nameContains(String name) {
        return name != null && !name.isBlank() ? product.name.containsIgnoreCase(name) : null;
    }

    // 정렬 조건 설정
    private OrderSpecifier<?>[] getOrderSpecifiers(Pageable pageable) {
        List<OrderSpecifier<?>> orders = new ArrayList<>();

        if (pageable.getSort().isEmpty()) {
            orders.add(product.createdAt.desc());
        } else {
            for (Sort.Order order : pageable.getSort()) {
                switch (order.getProperty()) {
                    case "createdAt" -> orders.add(order.isAscending() ? product.createdAt.asc() : product.createdAt.desc());
                    case "price" -> orders.add(order.isAscending() ? product.price.asc() : product.price.desc());
                    case "name" -> orders.add(order.isAscending() ? product.name.asc() : product.name.desc());
                    case "status" -> orders.add(order.isAscending() ? product.status.asc() : product.status.desc());
                    case "quantity" -> orders.add(order.isAscending() ? product.quantity.asc() : product.quantity.desc());
                    default -> orders.add(product.createdAt.desc());
                }
            }
        }

        return orders.toArray(new OrderSpecifier[0]);
    }
}
