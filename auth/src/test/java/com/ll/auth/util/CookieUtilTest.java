package com.ll.auth.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletResponse;
import static org.assertj.core.api.Assertions.assertThat;
import java.util.List;

class CookieUtilTest {

    @Test
    @DisplayName("쿠키 생성")
    void generateCookie() {
        // 단순 생성이므로 작성하지않음
    }

    @Test
    @DisplayName("로그아웃 -> 쿠키 만료")
    void expiredAuthCookie() {
        //given
        MockHttpServletResponse response = new MockHttpServletResponse();
        //when
        CookieUtil.expiredAuthCookie(response);
        //then
        List<String> cookies = response.getHeaders(HttpHeaders.SET_COOKIE);

        assertThat(cookies).hasSize(3);

        assertThat(cookies)
                .anyMatch(cookie -> cookie.contains("accessToken="))
                .anyMatch(cookie -> cookie.contains("refreshToken="))
                .anyMatch(cookie -> cookie.contains("deviceCode="));

        assertThat(cookies)
                .allMatch(cookie -> cookie.contains("Max-Age=0"));
    }

    @Test
    void setTokenCookie() {

        //given
        MockHttpServletResponse response = new MockHttpServletResponse();
        String accessToken = "accessToken";
        String refreshToken = "refreshToken";

        //when
        CookieUtil.setTokenCookie(response,accessToken,refreshToken);

        //then
        List<String> cookies = response.getHeaders(HttpHeaders.SET_COOKIE);
        assertThat(cookies).hasSize(2);
        assertThat(cookies)
                .anyMatch(cookie -> cookie.contains("accessToken="))
                .anyMatch(cookie -> cookie.contains("refreshToken="));

    }
}