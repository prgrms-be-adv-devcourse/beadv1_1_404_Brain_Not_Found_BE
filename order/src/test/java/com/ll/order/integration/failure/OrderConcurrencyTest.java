package com.ll.order.integration.failure;

import com.ll.core.model.exception.BaseException;
import com.ll.order.domain.mock.controller.ProductMockController;
import com.ll.order.domain.model.entity.Order;
import com.ll.order.domain.model.enums.order.OrderStatus;
import com.ll.order.domain.model.enums.order.OrderType;
import com.ll.order.domain.model.enums.payment.PaidType;
import com.ll.order.domain.model.enums.user.AccountStatus;
import com.ll.order.domain.model.enums.user.Grade;
import com.ll.order.domain.model.enums.user.Role;
import com.ll.order.domain.model.enums.user.SocialProvider;
import com.ll.order.domain.model.vo.request.OrderDirectRequest;
import com.ll.order.domain.model.vo.response.order.OrderCreateResponse;
import com.ll.order.domain.model.vo.response.product.ProductResponse;
import com.ll.order.domain.model.vo.response.user.UserResponse;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("주문 동시성 및 락 테스트")
@Slf4j
class OrderConcurrencyTest extends BaseOrderIntegrationFailureTest {

    @Autowired
    private ProductMockController productMockController;

    private UserResponse user1;
    private UserResponse user2;
    private ProductResponse testProduct;

    @BeforeEach
    void setUp() {
        user1 = createTestUser();

    user2 = new UserResponse(
                2L,
                "USER-002",
                "test_social_id_2",
                SocialProvider.KAKAO,
                "user2@test.com",
                "사용자2",
                Role.USER,
                null,
                5L,
                Grade.BRONZE,
                AccountStatus.ACTIVE,
                null,
                null,
                null,
                null
        );

        testProduct = createTestProduct("PROD-001", 3, 10000);
    }

    @DisplayName("재고 동시성 테스트: 두 사용자가 동시에 재고 3개인 상품을 2개씩 주문 시 한 명만 성공")
    @Test
    @Transactional
    void concurrentOrderCreation_InventoryConcurrency_OneSuccessOneFailure() throws Exception {
        // given
        String productCode = "PROD-001";
        int orderQuantity = 2; // 각 사용자가 2개씩 주문

        log.info("========== 동시성 테스트 시작 ==========");
        int originalInventory = testProduct.quantity();
        log.info("상품 코드: {}", productCode);
        log.info("원래 재고량: {}개", originalInventory);
        log.info("각 사용자 주문 수량: {}개", orderQuantity);
        log.info("테스트 방식: 실제 재고 상태를 추적하여 동시성 제어 시뮬레이션");

        // Mock 설정
        when(userServiceClient.getUserByCode("USER-001")).thenReturn(user1);
        when(userServiceClient.getUserByCode("USER-002")).thenReturn(user2);
        when(productServiceClient.getProductByCode(productCode)).thenReturn(testProduct);

        // 동시성 제어를 위한 CountDownLatch
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch completionLatch = new CountDownLatch(2);

        // 실제 재고 상태 추적 (동시성 테스트를 위해 실제 재고 상태 기반으로 동작)
        AtomicInteger currentInventory = new AtomicInteger(testProduct.quantity()); // 초기 재고: 3개
        AtomicInteger inventoryDecreaseCallCount = new AtomicInteger(0);
        AtomicInteger successfulOrderCount = new AtomicInteger(0);
        AtomicInteger failedOrderCount = new AtomicInteger(0);

        // 스레드 정보 추적 (멀티 스레드 실행 확인용)
        Thread mainThread = Thread.currentThread();
        long mainThreadId = mainThread.threadId();
        AtomicInteger thread1Id = new AtomicInteger(0);
        AtomicInteger thread2Id = new AtomicInteger(0);
        final String[] thread1Name = new String[1];
        final String[] thread2Name = new String[1];

        when(productServiceClient.getProductByCode(productCode)).thenReturn(testProduct);
        
        // 재고 차감 Mock 설정 (실제 재고 상태 기반으로 동시성 테스트)
        doAnswer(invocation -> {
            inventoryDecreaseCallCount.incrementAndGet();
            String calledProductCode = invocation.getArgument(0);
            Integer calledQuantity = invocation.getArgument(1);
            log.info("[재고 차감 호출 추적 #{}] productCode: {}, quantity: {}", 
                    inventoryDecreaseCallCount.get(), calledProductCode, calledQuantity);
            
            // 동시성 제어: 원자적으로 재고 차감 시도
            int current = currentInventory.get();
            int newInventory = current - calledQuantity;
            
            log.info("[재고 차감 시도] 현재 재고: {}, 차감 수량: {}, 예상 재고: {}", 
                    current, calledQuantity, newInventory);
            
            // 재고 부족 체크 및 원자적 업데이트
            if (newInventory < 0) {
                log.warn("[재고 차감 실패] 재고 부족 - 현재 재고: {}, 요청 수량: {}", current, calledQuantity);
                throw new RuntimeException("재고 부족: 현재 재고 " + current + "개, 요청 수량 " + calledQuantity + "개");
            }
            
            // 원자적으로 재고 차감
            boolean updated = currentInventory.compareAndSet(current, newInventory);
            if (!updated) {
                // 다른 스레드가 이미 재고를 변경했음 - 재시도 또는 실패
                int retryCurrent = currentInventory.get();
                int retryNewInventory = retryCurrent - calledQuantity;
                if (retryNewInventory < 0) {
                    log.warn("[재고 차감 실패] 재고 부족 (재시도) - 현재 재고: {}, 요청 수량: {}", 
                            retryCurrent, calledQuantity);
                    throw new RuntimeException("재고 부족: 현재 재고 " + retryCurrent + "개, 요청 수량 " + calledQuantity + "개");
                }
                currentInventory.set(retryNewInventory);
                log.info("[재고 차감 성공 (재시도)] 현재 재고: {} → {}", retryCurrent, retryNewInventory);
            } else {
                log.info("[재고 차감 성공] 현재 재고: {} → {}", current, newInventory);
            }
            
            return null;
        }).when(productServiceClient).decreaseInventory(anyString(), anyInt());
        
        // 결제 Mock 설정 (재고 차감 성공한 주문만 결제 진행)
        doAnswer(invocation -> {
            log.info("[결제 처리] 예치금 결제 성공");
            return null;
        }).when(paymentServiceClient).requestDepositPayment(any());

        // when: 두 사용자가 동시에 주문 생성
        CompletableFuture<OrderCreateResponse> order1Future = CompletableFuture.supplyAsync(() -> {
            try {
                Thread currentThread = Thread.currentThread();
                long currentThreadId = currentThread.threadId();
                thread1Id.set((int) currentThreadId);
                thread1Name[0] = currentThread.getName();
                log.info("[사용자1] 주문 생성 시작 - userCode: USER-001, Thread: {} (ID: {})",
                        currentThread.getName(), currentThreadId);
                log.info("[사용자1] 메인 스레드와 다른 스레드에서 실행 중: {}",
                        currentThreadId != mainThreadId);
                startLatch.await(); // 시작 신호 대기

                OrderDirectRequest request1 = new OrderDirectRequest(
                        productCode,
                        orderQuantity,
                        "서울시 강남구",
                        OrderType.ONLINE,
                        PaidType.DEPOSIT,
                        null
                );

                log.info("[사용자1] 주문 요청 생성 완료 - productCode: {}, quantity: {}",
                        request1.productCode(), request1.quantity());
                log.info("[사용자1] 재고 검증 단계 시작");

                try {
                    OrderCreateResponse response = orderService.createDirectOrder(request1, "USER-001");
                    successfulOrderCount.incrementAndGet();
                    log.info("[사용자1] 주문 생성 성공 - orderCode: {}, orderId: {}",
                            response.orderCode(), response.id());
                    log.info("[사용자1] 주문 상태: {}", response.orderStatus());
                    return response;
                } catch (BaseException e) {
                    failedOrderCount.incrementAndGet();
                    log.error("[사용자1] 주문 생성 실패 - errorCode: {}, message: {}",
                            e.getErrorCode(), e.getMessage());
                    throw e;
                } catch (Exception e) {
                    failedOrderCount.incrementAndGet();
                    log.error("[사용자1] 주문 생성 실패 - 예외: {}", e.getMessage(), e);
                    throw e;
                } finally {
                    completionLatch.countDown();
                }
            } catch (Exception e) {
                log.error("[사용자1] 예외 발생", e);
                completionLatch.countDown();
                return null;
            }
        });

        CompletableFuture<OrderCreateResponse> order2Future = CompletableFuture.supplyAsync(() -> {
            try {
                Thread currentThread = Thread.currentThread();
                long currentThreadId = currentThread.threadId();
                thread2Id.set((int) currentThreadId);
                thread2Name[0] = currentThread.getName();
                log.info("[사용자2] 주문 생성 시작 - userCode: USER-002, Thread: {} (ID: {})",
                        currentThread.getName(), currentThreadId);
                log.info("[사용자2] 메인 스레드와 다른 스레드에서 실행 중: {}",
                        currentThreadId != mainThreadId);
                startLatch.await(); // 시작 신호 대기

                OrderDirectRequest request2 = new OrderDirectRequest(
                        productCode,
                        orderQuantity,
                        "서울시 서초구",
                        OrderType.ONLINE,
                        PaidType.DEPOSIT,
                        null
                );

                log.info("[사용자2] 주문 요청 생성 완료 - productCode: {}, quantity: {}",
                        request2.productCode(), request2.quantity());
                log.info("[사용자2] 재고 검증 단계 시작");

                try {
                    OrderCreateResponse response = orderService.createDirectOrder(request2, "USER-002");
                    successfulOrderCount.incrementAndGet();
                    log.info("[사용자2] 주문 생성 성공 - orderCode: {}, orderId: {}",
                            response.orderCode(), response.id());
                    log.info("[사용자2] 주문 상태: {}", response.orderStatus());
                    return response;
                } catch (BaseException e) {
                    failedOrderCount.incrementAndGet();
                    log.error("[사용자2] 주문 생성 실패 - errorCode: {}, message: {}",
                            e.getErrorCode(), e.getMessage());
                    throw e;
                } catch (Exception e) {
                    failedOrderCount.incrementAndGet();
                    log.error("[사용자2] 주문 생성 실패 - 예외: {}", e.getMessage(), e);
                    throw e;
                } finally {
                    completionLatch.countDown();
                }
            } catch (Exception e) {
                log.error("[사용자2] 예외 발생", e);
                completionLatch.countDown();
                return null;
            }
        });

        // 동시 시작
        log.info("========== 두 사용자 동시 주문 시작 ==========");
        startLatch.countDown(); // 두 스레드가 동시에 시작하도록 신호

        // 결과 대기 (최대 10초)
        try {
            order1Future.get(10, TimeUnit.SECONDS);
        } catch (Exception e) {
            // 예외는 로그로 이미 기록됨
        }

        try {
            order2Future.get(10, TimeUnit.SECONDS);
        } catch (Exception e) {
            // 예외는 로그로 이미 기록됨
        }

        boolean completed = completionLatch.await(10, TimeUnit.SECONDS);
        if (!completed) {
            log.error("타임아웃 발생: 모든 주문이 10초 내에 완료되지 않았습니다.");
            throw new AssertionError("타임아웃: 모든 주문이 10초 내에 완료되지 않았습니다.");
        }

        // then: 검증
        log.info("========== 테스트 결과 검증 ==========");
        log.info("메인 스레드: {} (ID: {})", mainThread.getName(), mainThreadId);
        log.info("사용자1 실행 스레드: {} (ID: {})", thread1Name[0], thread1Id.get());
        log.info("사용자2 실행 스레드: {} (ID: {})", thread2Name[0], thread2Id.get());
        log.info("멀티 스레드 실행 여부: {}", thread1Id.get() != thread2Id.get() &&
                thread1Id.get() != (int) mainThreadId && thread2Id.get() != (int) mainThreadId);
        log.info("성공한 주문 수: {}", successfulOrderCount.get());
        log.info("실패한 주문 수: {}", failedOrderCount.get());
        log.info("재고 차감 호출 횟수: {}번", inventoryDecreaseCallCount.get());

        // 멀티 스레드 실행 검증
        assertThat(thread1Id.get())
                .as("사용자1과 사용자2는 서로 다른 스레드에서 실행되어야 합니다")
                .isNotEqualTo(thread2Id.get());
        assertThat(thread1Id.get())
                .as("사용자1은 메인 스레드가 아닌 별도 스레드에서 실행되어야 합니다")
                .isNotEqualTo((int) mainThreadId);
        assertThat(thread2Id.get())
                .as("사용자2는 메인 스레드가 아닌 별도 스레드에서 실행되어야 합니다")
                .isNotEqualTo((int) mainThreadId);

        // 한 명만 성공하고 한 명은 실패해야 함
        assertThat(successfulOrderCount.get()).isEqualTo(1);
        assertThat(failedOrderCount.get()).isEqualTo(1);

        // 재고 차감은 2번 호출되어야 함 (두 사용자 모두 재고 차감 시도)
        assertThat(inventoryDecreaseCallCount.get()).isEqualTo(2);
        
        // 최종 재고 확인 (초기 3개 - 성공한 주문 2개 = 1개)
        int finalInventory = currentInventory.get();
        log.info("========== 상품 재고량 확인 ==========");
        log.info("원래 재고량: {}개", originalInventory);
        log.info("동시성 테스트 후 상품 재고량: {}개", finalInventory);
        log.info("재고 차감량: {}개 (성공한 주문 수량)", orderQuantity);
        log.info("재고 변화: {}개 → {}개 ({}개 차감)", 
                originalInventory, finalInventory, originalInventory - finalInventory);
        assertThat(finalInventory)
                .as("최종 재고는 초기 재고에서 성공한 주문 수량만큼 차감되어야 함")
                .isEqualTo(testProduct.quantity() - orderQuantity);

        // 성공한 주문과 실패한 주문 확인
        List<Order> orders = orderJpaRepository.findAll();
        assertThat(orders).hasSize(2); // 두 주문 모두 생성됨 (실패한 주문도 이력 추적을 위해 저장됨)

        Order successfulOrder = orders.stream()
                .filter(order -> order.getOrderStatus() == OrderStatus.COMPLETED)
                .findFirst()
                .orElseThrow(() -> new AssertionError("성공한 주문이 없습니다."));

        Order failedOrder = orders.stream()
                .filter(order -> order.getOrderStatus() == OrderStatus.FAILED)
                .findFirst()
                .orElseThrow(() -> new AssertionError("실패한 주문이 없습니다."));

        log.info("========== 최종 검증 결과 ==========");
        log.info("성공한 주문 - orderCode: {}, status: {}, buyerCode: {}",
                successfulOrder.getCode(), successfulOrder.getOrderStatus(), successfulOrder.getBuyerCode());
        log.info("실패한 주문 - orderCode: {}, status: {}, buyerCode: {}",
                failedOrder.getCode(), failedOrder.getOrderStatus(), failedOrder.getBuyerCode());

        // 성공한 주문은 COMPLETED 상태
        assertThat(successfulOrder.getOrderStatus()).isEqualTo(OrderStatus.COMPLETED);

        // 실패한 주문은 FAILED 상태이고, 재고 차감 실패 에러
        assertThat(failedOrder.getOrderStatus()).isEqualTo(OrderStatus.FAILED);

        // 재고 차감 호출 확인 (2번 호출되어야 함: 두 사용자 모두 재고 차감 시도)
        log.info("========== 재고 차감 호출 횟수 확인 ==========");
        verify(productServiceClient, times(2)).decreaseInventory(productCode, orderQuantity);
        log.info("재고 차감 호출 횟수: {}번 (검증 완료)", inventoryDecreaseCallCount.get());

        // 결제는 성공한 주문에만 1번 호출
        verify(paymentServiceClient, times(1)).requestDepositPayment(any());

        log.info("========== 최종 재고량 요약 ==========");
        log.info("원래 재고량: {}개", originalInventory);
        log.info("동시성 테스트 후 최종 재고량: {}개", finalInventory);
        log.info("재고 차감량: {}개", originalInventory - finalInventory);
    }

}

