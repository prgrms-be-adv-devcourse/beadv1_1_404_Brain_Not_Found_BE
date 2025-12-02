package com.ll.auth.exception;


import com.ll.core.model.exception.BaseException;

public class TokenNotProvidedException extends BaseException {

    public TokenNotProvidedException() {
        super(ErrorCode.TOKEN_NOT_PROVIDED);
    }
}
