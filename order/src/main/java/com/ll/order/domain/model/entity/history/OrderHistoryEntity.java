package com.ll.order.domain.model.entity.history;

import com.ll.core.model.persistence.BaseEntity;
import com.ll.order.domain.model.enums.order.OrderHistoryActionType;
import com.ll.order.domain.model.enums.order.OrderStatus;
import com.ll.order.domain.model.enums.order.OrderType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "order_histories")
public class OrderHistoryEntity extends BaseEntity {

    // 기본 식별자
    @Column(nullable = false)
    private Long orderId;

    @Column(nullable = false)
    private String orderCode;

    // 주문 정보
    @Column(nullable = false)
    private Long buyerId;

    @Column(nullable = false)
    private String buyerCode;

    @Column(nullable = false)
    private Integer totalPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderType orderType;

    // 상태 및 이력
    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    private OrderStatus previousStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus currentStatus;

    @Column(nullable = false)
    private LocalDateTime statusChangedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderHistoryActionType actionType;

    // 연관 정보
    @Column(nullable = true)
    private Long paymentId;

    @Column(nullable = true)
    private String paymentCode;

    @Column(columnDefinition = "TEXT", nullable = true)
    private String relatedOrderItemIds; // JSON 배열 형태로 저장

    // 메타데이터
    @Column(columnDefinition = "TEXT", nullable = true)
    private String reason;

    @Column(columnDefinition = "TEXT", nullable = true)
    private String errorMessage;

    @Column(columnDefinition = "TEXT", nullable = true)
    private String requestData; // JSON 형태로 저장

    @Column(columnDefinition = "TEXT", nullable = true)
    private String responseData; // JSON 형태로 저장

    @Column(nullable = true)
    private String createdBy; // 생성자 (시스템 또는 사용자)
}
