package com.ll.order.domain.mock.controller;

import com.ll.core.model.response.BaseResponse;
import com.ll.order.domain.model.vo.response.cart.CartItemInfo;
import com.ll.order.domain.model.vo.response.cart.CartItemsResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Cart Service Mock Controller
 * 로컬 개발 환경에서 장바구니 정보를 모킹하는 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/carts")
public class CartMockController {

    /**
     * 장바구니 조회 Mock API
     * GET /api/carts/cartItems
     * Header: X-User-Code
     */
    @GetMapping("/cartItems")
    public ResponseEntity<BaseResponse<CartItemsResponse>> getCartItems(
            @RequestHeader("X-User-Code") String userCode
    ) {
        log.info("Mock Cart Service - 장바구니 조회 요청: userCode={}", userCode);

        // USER-001의 장바구니에 PROD-001이 1개 담겨있는 더미 데이터 반환
        if ("USER-001".equals(userCode)) {
            CartItemsResponse cartResponse = new CartItemsResponse(
                    "CART-001",
                    10000, // 총 금액: 상품 가격(10000) * 수량(1) = 10000
                    List.of(
                            new CartItemInfo(
                                    "CART-ITEM-001",
                                    1L, // productId (PROD-001의 id)
                                    "PROD-001",
                                    1, // 수량
                                    10000 // 총 가격 (상품 가격 * 수량)
                            )
                    )
            );

            log.info("Mock Cart Service - 장바구니 조회 성공: userCode={}, items={}", userCode, cartResponse.items().size());
            return BaseResponse.ok(cartResponse);
        }

        // 존재하지 않는 사용자의 장바구니
        log.warn("Mock Cart Service - 장바구니를 찾을 수 없음: userCode={}", userCode);
        return BaseResponse.error(com.ll.core.model.exception.ErrorCode.NOT_FOUND);
    }
}

