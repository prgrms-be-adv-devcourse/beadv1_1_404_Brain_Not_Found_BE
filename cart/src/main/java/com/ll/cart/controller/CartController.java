package com.ll.cart.controller;

import com.ll.cart.model.vo.request.CartItemAddRequest;
import com.ll.cart.model.vo.response.cart.CartItemAddResponse;
import com.ll.cart.model.vo.response.cart.CartItemRemoveResponse;
import com.ll.cart.model.vo.response.cart.CartItemsResponse;
import com.ll.cart.service.CartService;
import com.ll.core.model.response.BaseResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/carts")
public class CartController {

    private final CartService cartService;

    @PostMapping("/cartItems")
    public ResponseEntity<BaseResponse<CartItemAddResponse>> addCartItem(
            @RequestHeader("X-User-Code") String userCode,
            @Valid @RequestBody CartItemAddRequest request
    ) {
        CartItemAddResponse response = cartService.addCartItem(userCode, request);

        return BaseResponse.ok(response);
    }

    @DeleteMapping("/cartItems/{cartItemCode}")
    public ResponseEntity<BaseResponse<CartItemRemoveResponse>> removeCartItem(
            @PathVariable String cartItemCode,
            @RequestHeader("X-User-Code") String userCode
    ) {
        CartItemRemoveResponse response = cartService.removeCartItem(userCode, cartItemCode);

        return BaseResponse.ok(response);
    }

    @GetMapping("/cartItems")
    public ResponseEntity<BaseResponse<CartItemsResponse>> getCartItems(
            @RequestHeader("X-User-Code") String userCode
    ) {
        CartItemsResponse response = cartService.getCartItems(userCode);
        return BaseResponse.ok(response);
    }

}
