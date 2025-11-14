package com.ll.cart.service;

import com.ll.cart.dto.request.CartItemAddRequest;

public interface CartService {
    void addCartItem(String userCode, CartItemAddRequest request);

    void removeCartItem(String cartItemCode);
}
