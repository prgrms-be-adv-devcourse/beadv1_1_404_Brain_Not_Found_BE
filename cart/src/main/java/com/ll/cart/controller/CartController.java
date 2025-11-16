package com.ll.cart.controller;

import com.example.core.model.response.BaseResponse;
import com.ll.cart.model.vo.request.CartItemAddRequest;
import com.ll.cart.model.vo.response.CartItemAddResponse;
import com.ll.cart.model.vo.response.CartItemRemoveResponse;
import com.ll.cart.model.vo.response.CartItemsResponse;
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

    @DeleteMapping("/{cartCode}/cartItems/{cartItemCode}")
    public ResponseEntity<BaseResponse<CartItemRemoveResponse>> removeCartItem(
            @PathVariable String cartCode,
            @PathVariable String cartItemCode,
            @RequestHeader("userCode") String userCode
    ) {
        CartItemRemoveResponse response = cartService.removeCartItem(userCode, cartCode, cartItemCode);

        return BaseResponse.ok(response);
    }

    @GetMapping("/cartItems/{cartCode}")
    public ResponseEntity<BaseResponse<CartItemsResponse>> getCartItems(
            @PathVariable String cartCode,
            @RequestHeader("userCode") String userCode
    ) {
        CartItemsResponse response = cartService.getCartItems(cartCode, userCode);
        return BaseResponse.ok(response);
    }

    // 장바구니 주문 완료 처리 - 주문 생성 api에서 주문 완료까지 처라하는걸로 현재는 구현돼있고, 장바구니에 넣어야 할 지는 고민중.
//    @PostMapping("/{cartCode}/complete")
//    public ResponseEntity<BaseResponse<Void>> completeCart(@PathVariable String cartCode) {
//        return BaseResponse.ok(null);
//    }

}
