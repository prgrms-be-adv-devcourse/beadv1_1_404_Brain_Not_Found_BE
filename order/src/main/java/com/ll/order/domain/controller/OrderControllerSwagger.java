package com.ll.order.domain.controller;

import com.ll.core.model.response.BaseResponse;
import com.ll.order.domain.model.vo.request.OrderCartItemRequest;
import com.ll.order.domain.model.vo.request.OrderDirectRequest;
import com.ll.order.domain.model.vo.request.OrderStatusUpdateRequest;
import com.ll.order.domain.model.vo.response.order.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Map;

@Tag(name = "주문 관리", description = "주문 생성, 조회, 상태 변경 등 주문 관련 API")
public interface OrderControllerSwagger {

    @Operation(
            summary = "장바구니 상품 주문 생성",
            description = """
                    장바구니에 담긴 상품들을 주문으로 생성합니다.
                    예치금 결제는 즉시 결제 완료되며, 토스 결제는 결제 페이지로 리다이렉트됩니다.
                    """
    )
    Object createCartItemOrder(
            @Parameter(description = "주문 요청 정보", required = true) @Valid @RequestBody OrderCartItemRequest request,
            @Parameter(description = "사용자 코드", required = true) @RequestHeader("X-User-Code") String userCode
    );

    @Operation(
            summary = "직접 주문 생성",
            description = """
                    상품을 직접 선택하여 주문을 생성합니다.
                    예치금 결제는 즉시 결제 완료되며, 토스 결제는 결제 페이지로 리다이렉트됩니다.
                    """
    )
    Object createDirectOrder(
            @Parameter(description = "주문 요청 정보", required = true) @Valid @RequestBody OrderDirectRequest request,
            @Parameter(description = "사용자 코드", required = true) @RequestHeader("X-User-Code") String userCode
    );

    @Operation(
            summary = "주문 목록 조회",
            description = """
                    사용자의 주문 목록을 페이지네이션으로 조회합니다.
                    키워드 파라미터로 상품명 검색이 가능합니다.
                    """
    )
    ResponseEntity<BaseResponse<OrderPageResponse>> getOrderList(
            @Parameter(description = "사용자 코드", required = true) @RequestHeader("X-User-Code") String userCode,
            @Parameter(description = "검색 키워드 (상품명)") @RequestParam(required = false) String keyword,
            @Parameter(description = "페이지네이션 정보") @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    );

    @Operation(
            summary = "주문 상세 조회",
            description = "주문 코드를 사용하여 주문의 상세 정보를 조회합니다."
    )
    ResponseEntity<BaseResponse<OrderDetailResponse>> getOrderDetails(
            @Parameter(description = "주문 코드", required = true) @org.springframework.web.bind.annotation.PathVariable String orderCode,
            @Parameter(description = "사용자 코드", required = true) @RequestHeader("X-User-Code") String userCode
    );

    @Operation(
            summary = "주문 상태 변경",
            description = """
                    주문 상태를 변경합니다.
                    CANCELLED 상태로 변경 시 자동으로 환불 처리 및 재고 복구가 진행됩니다.
                    """
    )
    ResponseEntity<BaseResponse<OrderStatusUpdateResponse>> updateOrderStatus(
            @Parameter(description = "주문 코드", required = true) @org.springframework.web.bind.annotation.PathVariable String orderCode,
            @Parameter(description = "상태 변경 요청", required = true) @Valid @RequestBody OrderStatusUpdateRequest request,
            @Parameter(description = "사용자 코드", required = true) @RequestHeader("X-User-Code") String userCode
    );

    @Operation(
            summary = "주문 ID로 주문 코드 조회",
            description = "주문 ID를 입력받아 해당 주문의 주문 코드를 반환합니다."
    )
    ResponseEntity<BaseResponse<Map<String, String>>> getOrderCodeById(
            @Parameter(description = "주문 ID", required = true) @org.springframework.web.bind.annotation.PathVariable Long orderId
    );

    @Operation(
            summary = "결제 성공 콜백",
            description = """
                    토스 결제 성공 시 토스 서버에서 자동으로 호출되는 콜백 엔드포인트입니다.
                    결제 완료 처리 후 성공 페이지로 리다이렉트됩니다.
                    """
    )
    RedirectView paymentSuccess(
            @Parameter(description = "토스 결제 키", required = true) @RequestParam String paymentKey,
            @Parameter(description = "주문 ID", required = true) @RequestParam String orderId,
            @Parameter(description = "결제 금액", required = true) @RequestParam String amount
    );

    @Operation(
            summary = "결제 실패 콜백",
            description = """
                    토스 결제 실패 또는 취소 시 토스 서버에서 자동으로 호출되는 콜백 엔드포인트입니다.
                    실패 페이지로 리다이렉트되며 에러 정보를 전달합니다.
                    """
    )
    RedirectView paymentFail(
            @Parameter(description = "에러 코드") @RequestParam(required = false) String errorCode,
            @Parameter(description = "에러 메시지") @RequestParam(required = false) String errorMessage,
            @Parameter(description = "주문 ID") @RequestParam(required = false) String orderId
    );
}

