package com.ll.cart.controller;

import com.example.core.model.response.BaseResponse;
import com.ll.cart.dto.request.CartItemAddRequest;
import com.ll.cart.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @PostMapping("/api/cartitems/{userCode}")
    public ResponseEntity<BaseResponse<Void>> addCartItem(
            @PathVariable String userCode,
            @RequestBody CartItemAddRequest request
    ) {
        cartService.addCartItem(userCode, request);

        return BaseResponse.ok(null);
    }

    @DeleteMapping("/api/cartitems/{cartItemCode}")
    public ResponseEntity<BaseResponse<Void>> removeCartItem(
            @PathVariable String cartItemCode
    ) {
        cartService.removeCartItem(cartItemCode);

        return BaseResponse.ok(null);
    }

    @GetMapping("/api/carts/{cartCode}/cartItems")
    public ResponseEntity<BaseResponse<Void>> getCartItems(@PathVariable String cartCode) {
        return BaseResponse.ok(null);
    }

    @PostMapping("/api/carts/{cartCode}/complete")
    public ResponseEntity<BaseResponse<Void>> completeCart(@PathVariable String cartCode) {
        return BaseResponse.ok(null);
    }

}
