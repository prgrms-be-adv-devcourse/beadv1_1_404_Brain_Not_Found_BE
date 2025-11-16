package com.ll.cart.service;

import com.ll.cart.client.UserServiceClient;
import com.ll.cart.model.vo.request.CartItemAddRequest;
import com.ll.cart.model.vo.response.CartItemAddResponse;
import com.ll.cart.model.vo.response.CartItemRemoveResponse;
import com.ll.cart.model.vo.response.UserResponse;
import com.ll.cart.model.entity.Cart;
import com.ll.cart.model.entity.CartItem;
import com.ll.cart.model.enums.CartStatus;
import com.ll.cart.repository.CartItemRepository;
import com.ll.cart.repository.CartRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CartService 테스트")
class CartServiceImplTest {

    @Mock
    private UserServiceClient userServiceClient;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @InjectMocks
    private CartServiceImpl cartService;

    private UserResponse testUser;
    private Cart testCart;
    private CartItem testCartItem;

    @BeforeEach
    void setUp() {
        testUser = new UserResponse(1L, "홍길동", "서울시 강남구");
        testCart = new Cart(1L, CartStatus.ACTIVE);
        testCartItem = CartItem.create(testCart, 100L, 2, 20000);
    }

    @DisplayName("장바구니 아이템 추가 및 수량 업데이트 - 성공")
    @Test
    void addCartItem_NewItemAndUpdateQuantity_Success() {
        // given
        String userCode = "USER-001";
        String cartCode = "CART-001";
        CartItemAddRequest firstRequest = new CartItemAddRequest(100L, 2, 20000);
        CartItemAddRequest secondRequest = new CartItemAddRequest(100L, 5, 50000);

        when(userServiceClient.getUserByCode(userCode)).thenReturn(testUser);
        when(cartRepository.findByCodeAndStatus(cartCode, CartStatus.ACTIVE))
                .thenReturn(Optional.of(testCart));

        // 첫 번째 추가: 새로운 아이템 (빈 결과 반환)
        when(cartItemRepository.findByCartAndProductId(testCart, 100L))
                .thenReturn(Optional.empty());
        when(cartItemRepository.save(any(CartItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when - 첫 번째: 새로운 아이템 추가
        CartItemAddResponse firstResponse = cartService.addCartItem(userCode, cartCode, firstRequest);

        // then - 첫 번째 추가 검증
        assertThat(firstResponse.productId()).isEqualTo(100L);
        assertThat(firstResponse.quantity()).isEqualTo(2);
        assertThat(firstResponse.totalPrice()).isEqualTo(20000);
        assertThat(firstResponse.cartItemCode()).isNotNull();

        ArgumentCaptor<CartItem> cartItemCaptor = ArgumentCaptor.forClass(CartItem.class);
        verify(cartItemRepository, times(1)).save(cartItemCaptor.capture());
        CartItem savedCartItem = cartItemCaptor.getValue();

        assertThat(savedCartItem.getProductId()).isEqualTo(100L);
        assertThat(savedCartItem.getQuantity()).isEqualTo(2);
        assertThat(savedCartItem.getTotalPrice()).isEqualTo(20000);
        assertThat(savedCartItem.getCart()).isEqualTo(testCart);
        assertThat(testCart.getTotalPrice()).isEqualTo(20000);

        // 두 번째 추가: 기존 아이템 조회 (저장된 아이템 반환)
        CartItem existingCartItem = CartItem.create(testCart, 100L, 2, 20000);
        when(cartItemRepository.findByCartAndProductId(testCart, 100L))
                .thenReturn(Optional.of(existingCartItem));

        // when - 두 번째: 기존 아이템 수량 업데이트
        CartItemAddResponse secondResponse = cartService.addCartItem(userCode, cartCode, secondRequest);

        // then - 두 번째 업데이트 검증
        assertThat(secondResponse.productId()).isEqualTo(100L);
        assertThat(secondResponse.quantity()).isEqualTo(5);
        assertThat(secondResponse.totalPrice()).isEqualTo(50000);
        assertThat(existingCartItem.getQuantity()).isEqualTo(5);
        assertThat(existingCartItem.getTotalPrice()).isEqualTo(50000);
        assertThat(testCart.getTotalPrice()).isEqualTo(50000);

        verify(userServiceClient, times(2)).getUserByCode(userCode);
        verify(cartRepository, times(2)).findByCodeAndStatus(cartCode, CartStatus.ACTIVE);
        verify(cartItemRepository, times(2)).findByCartAndProductId(testCart, 100L);
        verify(cartItemRepository, times(1)).save(any(CartItem.class));
    }

    @DisplayName("장바구니에 아이템 추가 - 성공")
    @Test
    void addCartItem_Success() {
        // given
        String userCode = "USER-001";
        String cartCode = "CART-001";
        CartItemAddRequest request = new CartItemAddRequest(100L, 1, 10000);

        when(userServiceClient.getUserByCode(userCode)).thenReturn(testUser);
        when(cartRepository.findByCodeAndStatus(cartCode, CartStatus.ACTIVE))
                .thenReturn(Optional.of(testCart));
        when(cartItemRepository.findByCartAndProductId(testCart, 100L))
                .thenReturn(Optional.empty());
        when(cartItemRepository.save(any(CartItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        CartItemAddResponse response = cartService.addCartItem(userCode, cartCode, request);

        // then
        assertThat(response.productId()).isEqualTo(100L);
        assertThat(response.quantity()).isEqualTo(1);
        assertThat(response.totalPrice()).isEqualTo(10000);
        assertThat(response.cartItemCode()).isNotNull();

        ArgumentCaptor<CartItem> cartItemCaptor = ArgumentCaptor.forClass(CartItem.class);
        verify(cartItemRepository).save(cartItemCaptor.capture());
        CartItem savedCartItem = cartItemCaptor.getValue();

        assertThat(savedCartItem.getProductId()).isEqualTo(100L);
        assertThat(savedCartItem.getQuantity()).isEqualTo(1);
        assertThat(savedCartItem.getTotalPrice()).isEqualTo(10000);
        assertThat(testCart.getTotalPrice()).isEqualTo(10000);

        verify(userServiceClient).getUserByCode(userCode);
        verify(cartRepository).findByCodeAndStatus(cartCode, CartStatus.ACTIVE);
    }

    @DisplayName("장바구니 아이템 삭제 및 총액 차감 - 성공")
    @Test
    void removeCartItem_DecreaseTotalPrice_Success() throws Exception {
        // given
        String userCode = "USER-001";
        String cartCode = "CART-001";
        
        // testCart의 code를 cartCode로 설정
        Field codeField = testCart.getClass().getSuperclass().getDeclaredField("code");
        codeField.setAccessible(true);
        codeField.set(testCart, cartCode);
        
        CartItem cartItem1 = CartItem.create(testCart, 100L, 2, 20000);
        CartItem cartItem2 = CartItem.create(testCart, 200L, 1, 15000);
        testCart.increaseTotalPrice(20000);
        testCart.increaseTotalPrice(15000);

        when(userServiceClient.getUserByCode(userCode)).thenReturn(testUser);
        when(cartRepository.findByCodeAndStatus(cartCode, CartStatus.ACTIVE))
                .thenReturn(Optional.of(testCart));

        // 첫 번째 삭제: cartItem1 삭제
        String firstCartItemCode = "CART-ITEM-001";
        when(cartItemRepository.findByCodeAndCartStatus(firstCartItemCode, CartStatus.ACTIVE))
                .thenReturn(Optional.of(cartItem1));

        // when - 첫 번째 아이템 삭제
        CartItemRemoveResponse firstResponse = cartService.removeCartItem(userCode, cartCode, firstCartItemCode);

        // then - 첫 번째 삭제 검증
        assertThat(firstResponse.cartItemCode()).isEqualTo(firstCartItemCode);
        assertThat(firstResponse.productId()).isEqualTo(100L);
        assertThat(firstResponse.quantity()).isEqualTo(2);
        assertThat(firstResponse.totalPrice()).isEqualTo(20000);
        assertThat(testCart.getTotalPrice()).isEqualTo(15000);

        ArgumentCaptor<CartItem> cartItemCaptor = ArgumentCaptor.forClass(CartItem.class);
        verify(cartItemRepository).delete(cartItemCaptor.capture());
        assertThat(cartItemCaptor.getValue()).isEqualTo(cartItem1);

        // 두 번째 삭제: cartItem2 삭제
        String secondCartItemCode = "CART-ITEM-002";
        when(cartItemRepository.findByCodeAndCartStatus(secondCartItemCode, CartStatus.ACTIVE))
                .thenReturn(Optional.of(cartItem2));

        // when - 두 번째 아이템 삭제
        CartItemRemoveResponse secondResponse = cartService.removeCartItem(userCode, cartCode, secondCartItemCode);

        // then - 두 번째 삭제 검증
        assertThat(secondResponse.cartItemCode()).isEqualTo(secondCartItemCode);
        assertThat(secondResponse.productId()).isEqualTo(200L);
        assertThat(secondResponse.quantity()).isEqualTo(1);
        assertThat(secondResponse.totalPrice()).isEqualTo(15000);
        assertThat(testCart.getTotalPrice()).isEqualTo(0);

        verify(cartItemRepository, times(2)).delete(cartItemCaptor.capture());
        assertThat(cartItemCaptor.getAllValues()).containsExactly(cartItem1, cartItem2);

        verify(cartItemRepository).findByCodeAndCartStatus(firstCartItemCode, CartStatus.ACTIVE);
        verify(cartItemRepository).findByCodeAndCartStatus(secondCartItemCode, CartStatus.ACTIVE);
        verify(cartRepository, times(2)).findByCodeAndStatus(cartCode, CartStatus.ACTIVE);
        verify(userServiceClient, times(2)).getUserByCode(userCode);
    }
}

