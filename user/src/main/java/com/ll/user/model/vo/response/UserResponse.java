package com.ll.user.model.vo.response;

import com.ll.user.model.entity.User;
import com.ll.user.model.enums.AccountStatus;
import com.ll.user.model.enums.Grade;
import com.ll.user.model.enums.Role;
import com.ll.user.model.enums.SocialProvider;

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
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getCode(),
                user.getSocialId(),
                user.getSocialProvider(),
                user.getEmail(),
                user.getName(),
                user.getRole(),
                user.getProfileImageUrl(),
                user.getMannerScore(),
                user.getGrade(),
                user.getAccountStatus(),
                user.getAccountBank(),
                user.getAccountNumber(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
