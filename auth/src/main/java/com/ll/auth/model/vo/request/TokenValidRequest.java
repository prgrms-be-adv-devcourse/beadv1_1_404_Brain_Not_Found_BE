package com.ll.auth.model.vo.request;

public record TokenValidRequest (
        String refreshToken,
        String deviceCode
){

}
