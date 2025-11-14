package com.ll.auth.model.vo.response;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        UserLoginResponse user
) {
}
