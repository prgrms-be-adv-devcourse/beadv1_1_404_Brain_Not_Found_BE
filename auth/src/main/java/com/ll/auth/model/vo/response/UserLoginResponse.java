package com.ll.auth.model.vo.response;

import com.ll.auth.model.enums.AccountStatus;
import com.ll.auth.model.enums.Grade;
import com.ll.auth.model.enums.Role;
import com.ll.auth.model.enums.SocialProvider;

import java.time.LocalDateTime;

public record UserLoginResponse (
        String socialId,
        String code,
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
