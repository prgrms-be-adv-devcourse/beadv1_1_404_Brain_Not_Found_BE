package com.ll.auth.model.vo.response;

import com.ll.common.model.vo.response.UserLoginResponse;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        UserLoginResponse user
) {
}
