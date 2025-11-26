package com.ll.user.model.vo.request;

import com.ll.user.model.enums.SocialProvider;
import jakarta.validation.constraints.NotNull;



public record UserLoginRequest(
        @NotNull
        String socialId,
        @NotNull
        SocialProvider socialProvider,
        @NotNull
        String email,
        @NotNull
        String name
) {

}
