package com.ll.cart.service;

import com.ll.cart.dto.request.CartItemAddRequest;
import com.ll.cart.dto.response.CartItemAddResponse;
import com.ll.cart.dto.response.CartItemRemoveResponse;

public interface CartService {
    CartItemAddResponse addCartItem(String userCode, String cartCode, CartItemAddRequest request);

    CartItemRemoveResponse removeCartItem(String userCode, String cartItemCode);
}
