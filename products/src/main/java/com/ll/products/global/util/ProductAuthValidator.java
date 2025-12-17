package com.ll.products.global.util;

import com.ll.products.domain.cart.model.enums.Role;
import com.ll.products.global.exception.ProductAuthException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ProductAuthValidator {

    public static void validateSellerOrAdmin(String role) {
        if (!Role.SELLER.name().equals(role) && !Role.ADMIN.name().equals(role)) {
            throw new ProductAuthException("권한이 없습니다. SELLER 또는 ADMIN 권한이 필요합니다.");
        }
    }

    public static void validateAdmin(String role) {
        if (!Role.ADMIN.name().equals(role)) {
            throw new ProductAuthException("권한이 없습니다. ADMIN 권한이 필요합니다.");
        }
    }

    public static void validateOwnership(String ownerCode, String userCode, String role) {
        if (Role.ADMIN.name().equals(role)) {
            return;
        }
        if (!userCode.equals(ownerCode)) {
            throw new ProductAuthException("해당 상품에 대한 권한이 없습니다.");
        }
    }
}