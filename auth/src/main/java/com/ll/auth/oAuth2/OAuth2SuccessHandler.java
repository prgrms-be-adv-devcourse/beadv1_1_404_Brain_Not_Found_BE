package com.ll.auth.oAuth2;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.uuid.Generators;
import com.ll.auth.model.vo.dto.Tokens;
import com.ll.auth.service.AuthService;
import com.ll.auth.service.RedisService;
import com.ll.auth.util.CookieUtil;
import com.ll.common.model.vo.request.UserLoginRequest;
import com.ll.common.model.vo.response.UserLoginResponse;
import com.ll.user.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final JWTProvider jwtProvider;
    private final ObjectMapper objectMapper;
    private final OAuth2UserFactory oAuth2UserFactory;
    private final UserService userService;
    private final RedisService redisService;
    private final AuthService authService;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {

        UserLoginRequest loginRequest = oAuth2UserFactory.getOAuth2UserInfo(authentication);
        UserLoginResponse user = userService.createOrUpdateUser(loginRequest);

        // JWT 발급
        Tokens tokens = jwtProvider.createToken(user.code(),user.role().name());
        String accessToken = tokens.accessToken().trim();
        String refreshToken = tokens.refreshToken().trim();

        int accessTokenMaxAge = 60 * 15; // 15분
        int refreshTokenMaxAge = 60 * 60 * 24 * 7; // 7일

        // Cookie 생성
        ResponseCookie accessTokenCookie = CookieUtil.generateCookie("accessToken",accessToken,accessTokenMaxAge);
        ResponseCookie refreshTokenCookie = CookieUtil.generateCookie("refreshToken",refreshToken,refreshTokenMaxAge);
        String deviceCode = getDeviceCode(request);
        response.addHeader(HttpHeaders.SET_COOKIE,accessTokenCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE,refreshTokenCookie.toString());
        Cookie deviceCodeCookie = new Cookie("deviceCode",deviceCode);
        deviceCodeCookie.setPath("/");
        response.addCookie(deviceCodeCookie);

        //RefreshToken 저장
        redisService.saveRefreshToken(user.code(),deviceCode,refreshToken);
        authService.asyncSave(user.code(),deviceCode,refreshToken);
        //User 정보 Client에 제공
        Map<String, Object> body = Map.of(
                "user", Map.of(
                        "userCode", user.code(),
                        "email", user.email(),
                        "name", user.name(),
                        "role", user.role().name(),
                        "socialProvider", user.socialProvider().name()
                )
        );
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_OK);
        String json = objectMapper.writeValueAsString(body);
        response.getWriter().write(json);
        response.getWriter().flush();
    }


    private String getDeviceCode(HttpServletRequest request) {

        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("deviceCode".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return Generators.timeBasedEpochGenerator().generate().toString();

    }
}
