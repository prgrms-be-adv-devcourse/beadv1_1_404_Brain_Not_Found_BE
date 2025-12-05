package com.ll.products.domain.cart.model.vo.response;

import com.ll.products.domain.cart.model.entity.Cart;
import com.ll.products.domain.cart.model.entity.CartItem;
import com.ll.products.domain.product.model.entity.Product;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public record CartItemsResponse(
        String cartCode,
        Integer cartTotalPrice,
        List<CartItemInfo> items
) {
    public static CartItemsResponse from(Cart cart, List<CartItem> cartItems, List<Product> products) {
        // Product를 productId로 매핑하여 빠른 조회 가능하도록 Map 생성
        Map<Long, String> productCodeMap = products.stream()
                .collect(Collectors.toMap(
                        Product::getId,
                        Product::getCode
                ));
        
        List<CartItemInfo> itemInfos = cartItems.stream()
                .map(cartItem -> {
                    String productCode = productCodeMap.getOrDefault(cartItem.getProductId(), null);
                    return CartItemInfo.from(cartItem, productCode);
                })
                .collect(Collectors.toList());

        return new CartItemsResponse(
                cart.getCode(),
                cart.getTotalPrice(),
                itemInfos
        );
    }
}


