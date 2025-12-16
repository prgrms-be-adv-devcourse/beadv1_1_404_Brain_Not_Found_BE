package com.ll.products.domain.cart.controller;

import com.ll.core.model.response.BaseResponse;
import com.ll.products.domain.cart.model.vo.request.CartItemAddRequest;
import com.ll.products.domain.cart.model.vo.response.CartItemAddResponse;
import com.ll.products.domain.cart.model.vo.response.CartItemRemoveResponse;
import com.ll.products.domain.cart.model.vo.response.CartItemsResponse;
import com.ll.products.domain.cart.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Cart", description = "장바구니 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/carts")
public class CartController {

    private final CartService cartService;

    @Operation(summary = "장바구니 상품 추가")
    @PostMapping("/cartItems")
    public ResponseEntity<BaseResponse<CartItemAddResponse>> addCartItem(
            @RequestHeader("X-User-Code") String userCode,
            @Valid @RequestBody CartItemAddRequest request
    ) {
        CartItemAddResponse response = cartService.addCartItem(userCode, request);

        return BaseResponse.ok(response);
    }

    @Operation(summary = "장바구니 상품 삭제")
    @DeleteMapping("/cartItems/{cartItemCode}")
    public ResponseEntity<BaseResponse<CartItemRemoveResponse>> removeCartItem(
            @PathVariable String cartItemCode,
            @RequestHeader("X-User-Code") String userCode
    ) {
        CartItemRemoveResponse response = cartService.removeCartItem(userCode, cartItemCode);

        return BaseResponse.ok(response);
    }

    @Operation(summary = "장바구니 상품 조회")
    @GetMapping("/cartItems")
    public ResponseEntity<BaseResponse<CartItemsResponse>> getCartItems(
            @RequestHeader("X-User-Code") String userCode
    ) {
        CartItemsResponse response = cartService.getCartItems(userCode);
        return BaseResponse.ok(response);
    }

}
