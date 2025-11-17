package com.ll.cart.service;

import com.ll.cart.client.UserServiceClient;
import com.ll.cart.model.entity.Cart;
import com.ll.cart.model.entity.CartItem;
import com.ll.cart.model.enums.CartStatus;
import com.ll.cart.model.vo.request.CartItemAddRequest;
import com.ll.cart.model.vo.response.CartItemAddResponse;
import com.ll.cart.model.vo.response.CartItemInfo;
import com.ll.cart.model.vo.response.CartItemRemoveResponse;
import com.ll.cart.model.vo.response.CartItemsResponse;
import com.ll.cart.model.vo.response.UserResponse;
import com.ll.cart.repository.CartItemRepository;
import com.ll.cart.repository.CartRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartServiceImpl implements CartService {

    private final UserServiceClient userServiceClient;

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;

    @Override
    @Transactional
    public CartItemAddResponse addCartItem(String userCode, CartItemAddRequest request) {
//        validateCartItemRequest(request);

        UserResponse user = userServiceClient.getUserByCode(userCode);

        Cart cart = getOrCreateCart(user.id());

        Optional<CartItem> existingCartItem = cartItemRepository.findByCartAndProductId(cart, request.productId());

        CartItem cartItem;
        if (existingCartItem.isPresent()) { // 기존 아이템이 있으면 수량 업데이트
            cartItem = existingCartItem.get();
            int previousTotal = cartItem.getTotalPrice();
            cartItem.changeQuantity(request.quantity(), request.price());
            int difference = cartItem.getTotalPrice() - previousTotal;
            adjustCartTotalPrice(cart, difference);
        } else { // 기존 아이템이 없으면 새로운 아이템 생성
            cartItem = CartItem.create(
                    cart,
                    request.productId(),
                    request.quantity(),
                    request.price()
            );
            cartItemRepository.save(cartItem); // 새로운 아이템 저장
            cart.increaseTotalPrice(cartItem.getTotalPrice());
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
        UserResponse user = userServiceClient.getUserByCode(userCode);

        Cart cart = cartRepository.findByUserIdAndStatus(user.id(), CartStatus.ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException("장바구니를 찾을 수 없거나 활성 상태가 아닙니다."));

        CartItem cartItem = cartItemRepository.findByCodeAndCartStatus(cartItemCode, CartStatus.ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException("장바구니 아이템을 찾을 수 없거나 활성 상태의 장바구니가 아닙니다: " + cartItemCode));

        if (!cartItem.getCart().getId().equals(cart.getId())) {
            throw new IllegalArgumentException("해당 장바구니에 속하지 않은 아이템입니다.");
        }

        cart.decreaseTotalPrice(cartItem.getTotalPrice());
        
        // 삭제 전 정보 저장
        Long productId = cartItem.getProductId();
        Integer quantity = cartItem.getQuantity();
        Integer totalPrice = cartItem.getTotalPrice();
        
        cartItemRepository.delete(cartItem);

        return new CartItemRemoveResponse(
                cartItemCode,
                productId,
                quantity,
                totalPrice
        );
    }

    @Override
    public CartItemsResponse getCartItems(String userCode) {
        UserResponse user = userServiceClient.getUserByCode(userCode);

        Cart cart = cartRepository.findByUserIdAndStatus(user.id(), CartStatus.ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException("장바구니를 찾을 수 없거나 활성 상태가 아닙니다."));

        List<CartItem> items = cartItemRepository.findByCart(cart);

        List<CartItemInfo> itemInfos = items.stream()
                .map(ci -> new CartItemInfo(
                        ci.getCode(),
                        ci.getProductId(),
                        ci.getQuantity(),
                        ci.getTotalPrice()
                ))
                .collect(Collectors.toList());

        return new CartItemsResponse(
                cart.getCode(),
                cart.getTotalPrice(),
                itemInfos
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

}
