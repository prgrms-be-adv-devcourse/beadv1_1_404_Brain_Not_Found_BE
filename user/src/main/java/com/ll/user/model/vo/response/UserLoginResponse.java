package com.ll.user.model.vo.response;

import com.ll.user.model.enums.AccountStatus;
import com.ll.user.model.enums.Grade;
import com.ll.user.model.enums.Role;
import com.ll.user.model.enums.SocialProvider;

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
