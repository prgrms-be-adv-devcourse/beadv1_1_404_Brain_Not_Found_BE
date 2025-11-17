package com.ll.core.util;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.core.types.dsl.PathBuilder;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.*;

public class QueryDslSortUtil {

    private static final String DEFAULT_SORT_FIELD = "createdAt";

    public static <T> OrderSpecifier<?>[] getOrderSpecifiers(Pageable pageable, EntityPathBase<T> qEntity) {
        return getOrderSpecifiers(pageable, qEntity, true);
    }
    public static <T> OrderSpecifier<?>[] getOrderSpecifiers(Pageable pageable, EntityPathBase<T> qEntity, boolean applyDefaultCreatedDescSort) {
        if (pageable == null || pageable.getSort().isUnsorted()) {
            if (applyDefaultCreatedDescSort) {
                return new OrderSpecifier[]{
                        new OrderSpecifier<>(Order.DESC, new PathBuilder<>(qEntity.getType(), qEntity.getMetadata().getName()).getComparable(DEFAULT_SORT_FIELD, Comparable.class))
                };
            } else {
                return new OrderSpecifier[0];
            }
        }

        List<OrderSpecifier<?>> orders = new ArrayList<>();

        PathBuilder<T> pathBuilder =
                new PathBuilder<>(qEntity.getType(), qEntity.getMetadata().getName());

        for (Sort.Order order : pageable.getSort()) {
            Order direction = order.isAscending() ? Order.ASC : Order.DESC;
            orders.add(new OrderSpecifier<>(
                    direction,
                    pathBuilder.getComparable(order.getProperty(), Comparable.class)
            ));
        }

        return orders.toArray(OrderSpecifier[]::new);
    }

}
