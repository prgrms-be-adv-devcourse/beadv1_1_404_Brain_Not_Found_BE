package com.ll.order.integration.failure;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;

@DisplayName("카트 주문 실패 통합 테스트")
@Slf4j
class CartOrderIntegrationFailureTest extends BaseOrderIntegrationFailureTest {

    // ========== 카트 주문 실패 테스트 목록 ==========

    // ========== 2. 여러 상품 관련 실패 ==========
    // - 일부 상품 재고 부족: 여러 상품 중 일부만 재고 부족 → 부분 롤백 필요
    // - 일부 재고 차감 실패: 첫 번째 성공, 두 번째 실패 → 부분 롤백 필요
    // - 모든 상품 재고 차감 실패: 모든 상품 재고 부족 → 전체 실패

    // ========== 3. 카트 주문 특화 보상 로직 ==========
    // - 여러 상품 재고 롤백: 결제 실패 시 모든 상품 롤백 → 모든 상품에 대해 롤백 이벤트 발행
    // - 부분 롤백 실패: 일부 상품만 롤백 실패 → TransactionTracing에 실패 상태 저장
    // - 롤백 순서 검증: 여러 상품 롤백 순서 확인 → 롤백 이벤트 발행 순서 검증
    // - 롤백 이벤트 발행 실패: Kafka 연결 실패 시 보상 상태 저장 → CompensationService 호출

}

