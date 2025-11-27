package com.ll.user.exception;

import com.ll.core.model.exception.BaseException;
import com.ll.core.model.exception.ErrorCode;

public class UserNotFoundException extends BaseException {

    public UserNotFoundException(){
        super(ErrorCode.NOT_FOUND,"유저를 찾을 수 없습니다.");
    }
}
