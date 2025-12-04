package com.ll.auth.util;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;

public class CookieUtil {

    public static ResponseCookie generateCookie(String name, String data, int maxAge){
        return ResponseCookie.from(name,data)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(maxAge)
                .sameSite("Strict")
                .build();

    }
    public static ResponseCookie expiredCookie(String name){
        return generateCookie(name,null,0);
    }

    public static void setTokenCookie(HttpServletResponse response, String accessToken , String refreshToken){
        int accessTokenMaxAge = 60 * 15; // 15분
        int refreshTokenMaxAge = 60 * 60 * 24 * 7; // 7일
        ResponseCookie accessTokenCookie = CookieUtil.generateCookie("accessToken",accessToken,accessTokenMaxAge);
        ResponseCookie refreshTokenCookie = CookieUtil.generateCookie("refreshToken",refreshToken,refreshTokenMaxAge);
        response.addHeader(HttpHeaders.SET_COOKIE,accessTokenCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE,refreshTokenCookie.toString());
    }

    public static void expiredCookie(HttpServletResponse response){
        ResponseCookie accessTokenCookie = CookieUtil.expiredCookie("accessToken");
        ResponseCookie refreshTokenCookie = CookieUtil.expiredCookie("refreshToken");
        ResponseCookie deviceCodeCookie =  CookieUtil.expiredCookie("deviceCode");
        response.addHeader(HttpHeaders.SET_COOKIE,accessTokenCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE,refreshTokenCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE,deviceCodeCookie.toString());
    }
}
