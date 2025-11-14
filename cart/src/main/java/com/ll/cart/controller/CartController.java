package com.ll.cart.controller;

import com.example.core.model.response.BaseResponse;
import com.ll.cart.dto.request.CartItemAddRequest;
import com.ll.cart.dto.response.CartItemAddResponse;
import com.ll.cart.dto.response.CartItemRemoveResponse;
import com.ll.cart.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/carts")
public class CartController {

    private final CartService cartService;

    @PostMapping("/cartItems/{cartCode}")
    public ResponseEntity<BaseResponse<CartItemAddResponse>> addCartItem(
            @PathVariable String cartCode,
            @RequestHeader("userCode") String userCode,
            @RequestBody CartItemAddRequest request
    ) {
        CartItemAddResponse response = cartService.addCartItem(userCode, cartCode, request);

        return BaseResponse.ok(response);
    }

    @DeleteMapping("/cartItems/{cartItemCode}")
    public ResponseEntity<BaseResponse<CartItemRemoveResponse>> removeCartItem(
            @PathVariable String cartItemCode,
            @RequestHeader("userCode") String userCode
    ) {
        CartItemRemoveResponse response = cartService.removeCartItem(userCode, cartItemCode);

        return BaseResponse.ok(response);
    }

    @GetMapping("/cartItems/{cartCode}")
    public ResponseEntity<BaseResponse<Void>> getCartItems(@PathVariable String cartCode) {
        return BaseResponse.ok(null);
    }

    @PostMapping("/{cartCode}/complete")
    public ResponseEntity<BaseResponse<Void>> completeCart(@PathVariable String cartCode) {
        return BaseResponse.ok(null);
    }

}
