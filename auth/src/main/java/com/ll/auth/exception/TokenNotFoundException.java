package com.ll.auth.exception;


import com.ll.core.model.exception.BaseException;

public class TokenNotFoundException extends BaseException {

    public TokenNotFoundException() {
        super(ErrorCode.TOKEN_NOT_FOUND);
    }
}
