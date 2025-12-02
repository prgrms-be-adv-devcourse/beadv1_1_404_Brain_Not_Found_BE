package com.ll.common.model.vo.request;

import com.ll.common.model.enums.SocialProvider;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;


@Builder
public record UserLoginRequest(
        @NotNull(message = "socialId 는 필수입니다.")
        String socialId,
        @NotNull(message = "socialProvider 는 필수입니다.")
        SocialProvider socialProvider,
        @NotNull(message = "email은 필수입니다.")
        String email,
        @NotNull(message = "name은 필수입니다.")
        String name
) {

}
