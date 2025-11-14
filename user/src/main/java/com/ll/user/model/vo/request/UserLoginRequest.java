package com.ll.user.model.vo.request;

import com.ll.user.model.enums.SocialProvider;

public record UserLoginRequest(
        String socialId,
        SocialProvider socialProvider,
        String email,
        String name
) {

}
