package com.ll.auth.oAuth2;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ll.auth.client.UserServiceClient;
import com.ll.auth.model.entity.Auth;
import com.ll.auth.model.vo.request.UserLoginRequest;
import com.ll.auth.model.vo.dto.Tokens;
import com.ll.auth.model.vo.response.UserLoginResponse;
import com.ll.auth.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private final UserServiceClient userServiceClient;
    private final AuthService authService;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {

        UserLoginRequest loginRequest = oAuth2UserFactory.getOAuth2UserInfo(authentication);
        UserLoginResponse user = userServiceClient.requestUserLogin(loginRequest).getData();

        // JWT 발급
        Tokens tokens = jwtProvider.createToken(user.code());
        String accessToken = tokens.accessToken();
        String refreshToken = tokens.refreshToken();

        // Refresh Token 저장 (Redis 등)
        Auth auth = Auth.builder()
                .userCode(user.code())
                .refreshToken(refreshToken)
                .build();
        authService.save(auth);
        Map<String, Object> body = Map.of(
                "accessToken", accessToken,
                "refreshToken", refreshToken,
                "user", Map.of(
                        "userCode", user.code(),
                        "email", user.email(),
                        "name", user.name(),
                        "role", user.role().name(),
                        "socialProvider", user.socialProvider().name()
                )
        );

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        String json = objectMapper.writeValueAsString(body);
        response.getWriter().write(json);
        response.getWriter().flush();
    }
}
