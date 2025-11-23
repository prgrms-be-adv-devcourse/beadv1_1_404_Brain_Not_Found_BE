package com.ll.cart.model.vo.response.user;

import com.ll.cart.model.enums.AccountStatus;
import com.ll.cart.model.enums.Grade;
import com.ll.cart.model.enums.Role;
import com.ll.cart.model.enums.SocialProvider;

import java.time.LocalDateTime;

public record UserResponse (
        Long id,
        String code,
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
        LocalDateTime createAt,
        LocalDateTime updatedAt
){
}
