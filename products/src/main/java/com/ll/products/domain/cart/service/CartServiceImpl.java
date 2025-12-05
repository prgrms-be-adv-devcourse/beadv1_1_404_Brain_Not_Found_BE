package com.ll.products.domain.cart.service;

import com.ll.core.model.exception.BaseException;
import com.ll.core.model.vo.kafka.UserCreateEvent;
import com.ll.products.domain.cart.exception.CartErrorCode;
import com.ll.products.domain.cart.model.entity.Cart;
import com.ll.products.domain.cart.model.entity.CartItem;
import com.ll.products.domain.cart.model.enums.CartStatus;
import com.ll.products.domain.cart.model.vo.request.CartItemAddRequest;
import com.ll.products.domain.cart.model.vo.response.CartItemAddResponse;
import com.ll.products.domain.cart.model.vo.response.CartItemRemoveResponse;
import com.ll.products.domain.cart.model.vo.response.CartItemsResponse;
import com.ll.products.domain.cart.repository.CartItemRepository;
import com.ll.products.domain.cart.repository.CartRepository;
import com.ll.products.domain.product.model.entity.Product;
import com.ll.products.domain.product.repository.ProductRepository;
import com.ll.products.global.client.UserClient;
import com.ll.products.global.client.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartServiceImpl implements CartService {

    private final UserClient userServiceClient;

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;

    private final ProductRepository productRepository;

    @Override
    @Transactional
    public CartItemAddResponse addCartItem(String userCode, CartItemAddRequest request) {
        UserResponse user = userServiceClient.getUserByCode(userCode);

        Cart cart = getOrCreateCart(user.id());

        Product product = productRepository.findByIdAndIsDeleted(request.productId(), false)
                .orElseThrow(() -> new BaseException(CartErrorCode.PRODUCT_NOT_FOUND));
        validateProductPrice(request, product);
        Optional<CartItem> existingCartItem = cartItemRepository.findByCartAndProductId(cart, request.productId());
        CartItem cartItem;
        if (existingCartItem.isPresent()) { // 기존 아이템이 있으면 수량 업데이트
            cartItem = existingCartItem.get();
            validateProductStock(product, request.quantity());
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
            validateProductStock(product, cartItem.getQuantity());
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

        // CartItem의 productId로 Product 조회하여 productCode 매핑
        List<Long> productIds = items.stream()
                .map(CartItem::getProductId)
                .distinct()
                .toList();
        
        List<Product> products = productRepository.findAllById(productIds);
        
        return CartItemsResponse.from(cart, items, products);
    }

    @Override
    public void createCartForNewUser(UserCreateEvent event) {
        Cart cart = new Cart(event.userId(), CartStatus.ACTIVE);
        cartRepository.save(cart);
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

    private void validateProductStock(Product product, int requestedQuantity) {
        if (requestedQuantity > product.getQuantity()) {
            throw new BaseException(CartErrorCode.INSUFFICIENT_PRODUCT_STOCK);
        }
    }

    private static void validateProductPrice(CartItemAddRequest request, Product product) {
        if (!Objects.equals(request.price(), product.getPrice())) {
            throw new BaseException(CartErrorCode.PRODUCT_PRICE_MISMATCH);
        }
    }

}
