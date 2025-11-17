package com.ll.cart.model.vo.response;

import com.ll.cart.model.enums.AccountStatus;
import com.ll.cart.model.enums.Grade;
import com.ll.cart.model.enums.Role;
import com.ll.cart.model.enums.SocialProvider;

import java.time.LocalDateTime;

public record UserResponse(
        Long id,  // Cart 모듈에서만 필요한 필드
        String socialId,
        SocialProvider socialProvider,
        String email,
        String name,
        Role role,
        String profileImageUrl,
        Long mannerScore,
        Grade grade,
        AccountStatus accountStatus,
        String accountBank,
        String accountNumber,
        String address,
        LocalDateTime createAt,
        LocalDateTime updatedAt
) {
}

