package com.ll.auth.exception;

import com.example.core.exception.BaseException;
import com.example.core.exception.ErrorCode;

public class TokenNotFoundException extends BaseException {

    public TokenNotFoundException() {
        super(ErrorCode.NOT_FOUND,"리프레시 토큰을 찾을 수 없습니다");
    }
}
