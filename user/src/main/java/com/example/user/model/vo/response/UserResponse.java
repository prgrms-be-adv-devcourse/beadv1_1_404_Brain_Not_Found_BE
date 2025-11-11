package com.example.user.model.vo.response;

import com.example.user.model.enums.AccountStatus;
import com.example.user.model.enums.Grade;
import com.example.user.model.enums.Role;
import com.example.user.model.enums.SocialProvider;

import java.time.LocalDateTime;

public record UserResponse (
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
