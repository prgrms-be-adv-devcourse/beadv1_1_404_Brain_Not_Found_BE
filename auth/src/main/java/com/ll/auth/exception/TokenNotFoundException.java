package com.ll.auth.exception;


import com.ll.core.model.exception.BaseException;
import com.ll.core.model.exception.ErrorCode;

public class TokenNotFoundException extends BaseException {

    public TokenNotFoundException() {
        super(ErrorCode.NOT_FOUND,"리프레시 토큰을 찾을 수 없습니다");
    }
}
