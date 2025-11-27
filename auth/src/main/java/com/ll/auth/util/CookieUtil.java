package com.ll.auth.util;

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
}
