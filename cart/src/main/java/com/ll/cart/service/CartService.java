package com.ll.cart.service;

import com.ll.cart.model.vo.request.CartItemAddRequest;
import com.ll.cart.model.vo.response.CartItemAddResponse;
import com.ll.cart.model.vo.response.CartItemRemoveResponse;
import com.ll.cart.model.vo.response.CartItemsResponse;

public interface CartService {
    CartItemAddResponse addCartItem(String userCode, CartItemAddRequest request);

    CartItemRemoveResponse removeCartItem(String userCode, String cartItemCode);

    CartItemsResponse getCartItems(String userCode);
}
