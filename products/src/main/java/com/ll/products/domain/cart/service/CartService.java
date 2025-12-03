package com.ll.products.domain.cart.service;

import com.ll.core.model.vo.kafka.UserCreateEvent;
import com.ll.products.domain.cart.model.vo.request.CartItemAddRequest;
import com.ll.products.domain.cart.model.vo.response.CartItemAddResponse;
import com.ll.products.domain.cart.model.vo.response.CartItemRemoveResponse;
import com.ll.products.domain.cart.model.vo.response.CartItemsResponse;

public interface CartService {
    CartItemAddResponse addCartItem(String userCode, CartItemAddRequest request);

    CartItemRemoveResponse removeCartItem(String userCode, String cartItemCode);

    CartItemsResponse getCartItems(String userCode);

    void createCartForNewUser(UserCreateEvent event);
}
