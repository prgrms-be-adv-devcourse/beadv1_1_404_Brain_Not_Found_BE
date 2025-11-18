package com.ll.gateway.exception;


import lombok.Getter;

@Getter
public class GatewayBaseException extends RuntimeException {

    private final GatewayErrorCode errorCode;

    public GatewayBaseException(GatewayErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}

