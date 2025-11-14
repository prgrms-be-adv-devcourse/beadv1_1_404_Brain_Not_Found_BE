package com.ll.cart.service;

import com.ll.cart.dto.request.CartItemAddRequest;
import com.ll.cart.repository.CartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;

    @Override
    public void addCartItem(String userCode, CartItemAddRequest request) {

    }

    @Override
    public void removeCartItem(String cartItemCode) {

    }
}
