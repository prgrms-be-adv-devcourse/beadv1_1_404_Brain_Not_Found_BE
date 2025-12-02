package com.ll.auth.exception;


import com.ll.core.model.exception.BaseException;

public class DeviceCodeNotProvidedException extends BaseException {

    public DeviceCodeNotProvidedException() {
        super(ErrorCode.DEVICE_CODE_NOT_PROVIDED);
    }
}
