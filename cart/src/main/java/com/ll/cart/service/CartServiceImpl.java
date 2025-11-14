package com.ll.cart.service;

import com.ll.cart.client.UserServiceClient;
import com.ll.cart.dto.request.CartItemAddRequest;
import com.ll.cart.dto.response.CartItemAddResponse;
import com.ll.cart.dto.response.CartItemRemoveResponse;
import com.ll.cart.dto.response.UserResponse;
import com.ll.cart.model.Cart;
import com.ll.cart.model.CartItem;
import com.ll.cart.model.CartStatus;
import com.ll.cart.repository.CartItemRepository;
import com.ll.cart.repository.CartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final UserServiceClient userServiceClient;

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;

    @Override
    @Transactional
    public CartItemAddResponse addCartItem(String userCode, String cartCode, CartItemAddRequest request) {
        validateCartItemRequest(request);

        UserResponse user = userServiceClient.getUserByCode(userCode);

        // 장바구니 조회해서 없으면 생성
        Cart cart = cartRepository.findByCode(cartCode)
                .orElseGet(() -> getOrCreateCart(user.id()));

        if (!cart.getUserId().equals(user.id())) {
            throw new IllegalArgumentException("다른 사용자의 장바구니에는 아이템을 추가할 수 없습니다.");
        }

        // 장바구니 상태 검증
        validateCartStatus(cart);

        Optional<CartItem> existingCartItem = cartItemRepository.findByCartAndProductId(cart, request.getProductId());

        CartItem cartItem;
        if (existingCartItem.isPresent()) {
            cartItem = existingCartItem.get();
            int previousTotal = cartItem.getTotalPrice();
            cartItem.changeQuantity(request.getQuantity(), request.getTotalPrice());
            int difference = request.getTotalPrice() - previousTotal;
            adjustCartTotalPrice(cart, difference);
        } else {
            cartItem = CartItem.create(
                    cart,
                    request.getProductId(),
                    request.getQuantity(),
                    request.getTotalPrice()
            );
            cartItemRepository.save(cartItem);
            cart.increaseTotalPrice(request.getTotalPrice());
        }

        return new CartItemAddResponse(
                cartItem.getCode(),
                cartItem.getProductId(),
                cartItem.getQuantity(),
                cartItem.getTotalPrice()
        );
    }

    @Override
    @Transactional
    public CartItemRemoveResponse removeCartItem(String userCode, String cartItemCode) {
        // 사용자 정보 조회
        UserResponse user = userServiceClient.getUserByCode(userCode);

        // 장바구니 아이템 조회
        CartItem cartItem = cartItemRepository.findByCode(cartItemCode)
                .orElseThrow(() -> new IllegalArgumentException("장바구니 아이템을 찾을 수 없습니다: " + cartItemCode));

        Cart cart = cartItem.getCart();

        // 권한 검증: 삭제하려는 아이템이 해당 사용자의 장바구니에 속하는지 확인
        if (!cart.getUserId().equals(user.id())) {
            throw new IllegalArgumentException("다른 사용자의 장바구니 아이템은 삭제할 수 없습니다.");
        }

        // 장바구니 상태 검증
        validateCartStatus(cart);

        cart.decreaseTotalPrice(cartItem.getTotalPrice());
        cartItemRepository.delete(cartItem);

        return new CartItemRemoveResponse(
                cartItemCode,
                "장바구니 아이템이 삭제되었습니다."
        );
    }

    private Cart getOrCreateCart(Long userId) {
        return cartRepository.findByUserIdAndStatus(userId, CartStatus.ACTIVE)
                .orElseGet(() -> cartRepository.save(new Cart(userId, CartStatus.ACTIVE)));
    }

    private void adjustCartTotalPrice(Cart cart, int difference) {
        if (difference > 0) {
            cart.increaseTotalPrice(difference);
        } else if (difference < 0) {
            cart.decreaseTotalPrice(Math.abs(difference));
        }
    }

    private void validateCartItemRequest(CartItemAddRequest request) {
        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            throw new IllegalArgumentException("수량은 1 이상이어야 합니다.");
        }
        if (request.getTotalPrice() == null || request.getTotalPrice() < 0) {
            throw new IllegalArgumentException("총 가격은 0 이상이어야 합니다.");
        }
        if (request.getProductId() == null) {
            throw new IllegalArgumentException("상품 ID는 필수입니다.");
        }
    }

    private void validateCartStatus(Cart cart) {
        if (cart.getStatus() != CartStatus.ACTIVE) {
            throw new IllegalStateException("장바구니가 활성 상태가 아닙니다. 현재 상태: " + cart.getStatus());
        }
    }

}
