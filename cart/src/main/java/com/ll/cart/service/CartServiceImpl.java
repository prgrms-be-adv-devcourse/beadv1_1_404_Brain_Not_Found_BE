package com.ll.cart.service;

import com.ll.cart.client.UserServiceClient;
import com.ll.cart.exception.CartErrorCode;
import com.ll.cart.model.entity.Cart;
import com.ll.cart.model.entity.CartItem;
import com.ll.cart.model.enums.CartStatus;
import com.ll.cart.model.vo.request.CartItemAddRequest;
import com.ll.cart.model.vo.response.cart.CartItemAddResponse;
import com.ll.cart.model.vo.response.cart.CartItemRemoveResponse;
import com.ll.cart.model.vo.response.cart.CartItemsResponse;
import com.ll.cart.model.vo.response.user.UserResponse;
import com.ll.cart.repository.CartItemRepository;
import com.ll.cart.repository.CartRepository;
import com.ll.core.model.exception.BaseException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

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

        return CartItemAddResponse.from(cartItem);
    }

    @Override
    @Transactional
    public CartItemRemoveResponse removeCartItem(String userCode, String cartItemCode) {
        UserResponse user = userServiceClient.getUserByCode(userCode);

        Cart cart = findCartByUserIdAndStatus(user.id(), CartStatus.ACTIVE);

        CartItem cartItem = findCartItemByCodeAndStatus(cartItemCode, CartStatus.ACTIVE);

        if (!cartItem.getCart().getId().equals(cart.getId())) {
            throw new BaseException(CartErrorCode.CART_ITEM_NOT_BELONG_TO_CART);
        }

        cart.decreaseTotalPrice(cartItem.getTotalPrice());

        cartItemRepository.delete(cartItem);

        return CartItemRemoveResponse.from(cartItem);
    }

    @Override
    public CartItemsResponse getCartItems(String userCode) {
        UserResponse user = userServiceClient.getUserByCode(userCode);

        Cart cart = findCartByUserIdAndStatus(user.id(), CartStatus.ACTIVE);

        List<CartItem> items = cartItemRepository.findByCart(cart);

        return CartItemsResponse.from(cart, items);
    }

    private Cart getOrCreateCart(Long userId) {
        return cartRepository.findByUserIdAndStatus(userId, CartStatus.ACTIVE)
                .orElseGet(() -> cartRepository.save(new Cart(userId, CartStatus.ACTIVE)));
    }

    private Cart findCartByUserIdAndStatus(Long userId, CartStatus status) {
        return cartRepository.findByUserIdAndStatus(userId, status)
                .orElseThrow(() -> new BaseException(CartErrorCode.CART_NOT_FOUND));
    }

    private CartItem findCartItemByCodeAndStatus(String cartItemCode, CartStatus status) {
        return cartItemRepository.findByCodeAndCartStatus(cartItemCode, status)
                .orElseThrow(() -> {
                    log.warn("cartItemCode: {}", cartItemCode);
                    return new BaseException(CartErrorCode.CART_ITEM_NOT_FOUND);
                });
    }

    private void adjustCartTotalPrice(Cart cart, int difference) {
        if (difference > 0) {
            cart.increaseTotalPrice(difference);
        } else if (difference < 0) {
            cart.decreaseTotalPrice(Math.abs(difference));
        }
    }

}
