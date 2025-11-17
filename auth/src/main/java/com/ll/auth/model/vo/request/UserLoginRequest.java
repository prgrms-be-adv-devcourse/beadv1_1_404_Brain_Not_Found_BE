package com.ll.auth.model.vo.request;

import com.ll.auth.model.enums.SocialProvider;
import lombok.Builder;

@Builder
public record UserLoginRequest(
        String socialId,
        SocialProvider socialProvider,
        String email,
        String name
) {
}