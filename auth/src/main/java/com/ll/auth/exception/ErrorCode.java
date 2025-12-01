package com.ll.auth.exception;

import com.ll.core.model.exception.BaseErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode implements BaseErrorCode {
    TOKEN_NOT_PROVIDED(HttpStatus.BAD_REQUEST, "요청에 리프레시 토큰이 제공되지 않았습니다."),
    DEVICE_CODE_NOT_PROVIDED(HttpStatus.BAD_REQUEST, "요청에 장치 코드(deviceCode)가 제공되지 않았습니다."),

    TOKEN_NOT_FOUND(HttpStatus.NOT_FOUND, "리프레시 토큰을 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}
