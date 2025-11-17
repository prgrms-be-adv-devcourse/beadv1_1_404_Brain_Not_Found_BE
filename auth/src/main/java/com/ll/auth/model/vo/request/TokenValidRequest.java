package com.ll.auth.model.vo.request;

public record TokenValidRequest (
        String userCode,
        String role,
        String refreshToken
){

}
