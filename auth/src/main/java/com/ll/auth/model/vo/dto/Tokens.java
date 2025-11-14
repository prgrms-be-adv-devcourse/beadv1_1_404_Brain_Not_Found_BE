package com.ll.auth.model.vo.dto;

public record Tokens(
        String accessToken,
        String refreshToken
) {
}
