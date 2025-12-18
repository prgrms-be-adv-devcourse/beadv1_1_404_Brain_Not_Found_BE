package com.ll.gateway.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum GatewayErrorCode {

    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증 정보가 유효하지 않습니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN,"허용되지 않은 접근입니다."),
    SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE,"서비스 이용에 장애가 발생했습니다. 다시 시도해 주세요.");

    private final HttpStatus status;
    private final String message;

    GatewayErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}
