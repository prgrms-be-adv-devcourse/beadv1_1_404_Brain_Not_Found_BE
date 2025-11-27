package com.ll.order.domain.service;

import com.ll.core.model.exception.BaseException;
import com.ll.order.domain.client.ProductServiceClient;
import com.ll.order.domain.exception.OrderErrorCode;
import com.ll.order.domain.model.entity.Order;
import com.ll.order.domain.model.enums.order.OrderStatus;
import com.ll.order.domain.model.enums.product.ProductStatus;
import com.ll.order.domain.model.vo.request.ProductRequest;
import com.ll.order.domain.model.vo.response.order.OrderValidateResponse;
import com.ll.order.domain.model.vo.response.product.ProductResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderValidator {

    private final ProductServiceClient productServiceClient;

    public List<OrderValidateResponse.ItemInfo> validateProducts(List<ProductRequest> products) {
        Set<String> duplicatedCheck = new HashSet<>();
        List<OrderValidateResponse.ItemInfo> itemInfos = new ArrayList<>();

        for (ProductRequest productRequest : products) {
            if (!duplicatedCheck.add(productRequest.productCode())) {
                log.warn("중복된 상품 코드가 포함되어 있습니다. productCode: {}", productRequest.productCode());
                throw new BaseException(OrderErrorCode.DUPLICATE_PRODUCT_CODE);
            }

            ProductResponse productInfo = getProductInfo(productRequest.productCode());

            if (productInfo.quantity() < productRequest.quantity()) {
                log.warn("재고가 부족합니다. productCode: {}, 요청 수량: {}, 재고: {}",
                        productRequest.productCode(), productRequest.quantity(), productInfo.quantity());
                throw new BaseException(OrderErrorCode.INSUFFICIENT_INVENTORY);
            }

            if (productInfo.status() == null || productInfo.status() != ProductStatus.ON_SALE) {
                log.warn("판매 중이 아닌 상품입니다. productCode: {}, status: {}",
                        productRequest.productCode(), productInfo.status());
                throw new BaseException(OrderErrorCode.PRODUCT_NOT_ON_SALE);
            }

            if (productRequest.price() != productInfo.price()) {
                log.warn("요청한 상품 가격이 실제 가격과 일치하지 않습니다. productCode: {}, 요청 가격: {}, 실제 가격: {}",
                        productRequest.productCode(), productRequest.price(), productInfo.price());
                throw new BaseException(OrderErrorCode.PRODUCT_PRICE_MISMATCH);
            }

            itemInfos.add(OrderValidateResponse.ItemInfo.from(
                    productRequest.productCode(),
                    productRequest.quantity(),
                    productInfo.price()
            ));
        }

        return itemInfos;
    }

    public void validateOrderStatusTransition(OrderStatus current, OrderStatus target) {
        if (!current.canTransitionTo(target)) {
            log.warn("해당 상태로 전환할 수 없습니다. current: {}, target: {}", current, target);
            throw new BaseException(OrderErrorCode.INVALID_ORDER_STATUS_TRANSITION);
        }
    }

    public void validateOrderForPayment(Order order) {
        if (order.getOrderStatus() != OrderStatus.CREATED) {
            log.warn("이미 처리된 주문입니다. orderCode: {}, 현재 상태: {}",
                    order.getCode(), order.getOrderStatus());
            throw new BaseException(OrderErrorCode.ORDER_ALREADY_PROCESSED);
        }
    }

    private ProductResponse getProductInfo(String productCode) {
        return Optional.ofNullable(productServiceClient.getProductByCode(productCode))
                .orElseThrow(() -> {
                    log.warn("상품을 찾을 수 없습니다. productCode: {}", productCode);
                    return new BaseException(OrderErrorCode.PRODUCT_NOT_FOUND);
                });
    }
}

