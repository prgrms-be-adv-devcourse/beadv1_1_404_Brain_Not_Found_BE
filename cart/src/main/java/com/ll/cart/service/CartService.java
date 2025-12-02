package com.ll.cart.service;

import com.ll.cart.model.vo.request.CartItemAddRequest;
import com.ll.cart.model.vo.response.cart.CartItemAddResponse;
import com.ll.cart.model.vo.response.cart.CartItemRemoveResponse;
import com.ll.cart.model.vo.response.cart.CartItemsResponse;

public interface CartService {
    CartItemAddResponse addCartItem(String userCode, CartItemAddRequest request);

    CartItemRemoveResponse removeCartItem(String userCode, String cartItemCode);

    CartItemsResponse getCartItems(String userCode);
}
