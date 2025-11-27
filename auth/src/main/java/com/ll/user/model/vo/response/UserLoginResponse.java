package com.ll.user.model.vo.response;

import com.ll.user.model.entity.User;
import com.ll.user.model.enums.AccountStatus;
import com.ll.user.model.enums.Grade;
import com.ll.user.model.enums.Role;
import com.ll.user.model.enums.SocialProvider;

import java.time.LocalDateTime;

public record UserLoginResponse (
        Long id,
        String code,
        String socialId,
        SocialProvider socialProvider,
        String email,
        String name,
        Role role,
        Long mannerScore,
        Grade grade,
        AccountStatus accountStatus,
        LocalDateTime createAt
){
    public static UserLoginResponse from(User user) {
        return new UserLoginResponse(
                user.getId(),
                user.getCode(),
                user.getSocialId(),
                user.getSocialProvider(),
                user.getEmail(),
                user.getName(),
                user.getRole(),
                user.getMannerScore(),
                user.getGrade(),
                user.getAccountStatus(),
                user.getCreatedAt()
        );
    }
}
