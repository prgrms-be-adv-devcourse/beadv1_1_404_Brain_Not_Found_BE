package com.ll.order.domain.model.vo.response;

import com.ll.order.domain.model.enums.AccountStatus;
import com.ll.order.domain.model.enums.Grade;
import com.ll.order.domain.model.enums.Role;
import com.ll.order.domain.model.enums.SocialProvider;

import java.time.LocalDateTime;

public record UserResponse(
        Long id,
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
