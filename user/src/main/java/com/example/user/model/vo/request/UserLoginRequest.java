package com.example.user.model.vo.request;

import com.example.user.model.enums.SocialProvider;

public record UserLoginRequest(
        String socialId,
        SocialProvider socialProvider,
        String email,
        String name
) {

}
