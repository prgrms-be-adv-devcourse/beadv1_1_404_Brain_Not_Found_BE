package com.example.user.model.vo.response;

import com.example.user.model.enums.AccountStatus;
import com.example.user.model.enums.Grade;
import com.example.user.model.enums.Role;
import com.example.user.model.enums.SocialProvider;

import java.time.LocalDateTime;

public record UserLoginResponse (
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

}
