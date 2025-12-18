package com.ll.auth.controller;

import com.ll.auth.controller.swagger.LogoutApiResponse;
import com.ll.auth.controller.swagger.RefreshTokenApiResponse;
import com.ll.auth.util.CookieUtil;
import com.ll.core.model.response.BaseResponse;
import com.ll.auth.model.vo.dto.Tokens;
import com.ll.auth.model.vo.request.TokenValidRequest;
import com.ll.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Auth", description = "토큰 관리 API")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping
    @RefreshTokenApiResponse
    public ResponseEntity<BaseResponse<Tokens>> refreshToken(
            @Parameter(hidden = true)
            @CookieValue(name = "refreshToken", required = false) String refreshToken,
            @Parameter(hidden = true)
            @CookieValue(name = "deviceCode", required = false) String deviceCode,
            HttpServletResponse response
    ) {
        TokenValidRequest validRequest = new TokenValidRequest(refreshToken, deviceCode);
        Tokens tokens = authService.refreshToken(validRequest);
        CookieUtil.setTokenCookie(response, tokens.accessToken(), tokens.refreshToken());
        return BaseResponse.ok(tokens);
    }

    @PostMapping("/logout")
    @LogoutApiResponse
    public ResponseEntity<BaseResponse<Void>> logout(
            @Parameter(hidden = true)
            @CookieValue(name = "refreshToken", required = false) String refreshToken,
            @Parameter(hidden = true)
            @CookieValue(name = "deviceCode", required = false) String deviceCode,
            HttpServletResponse response
    ) {
        TokenValidRequest validRequest = new TokenValidRequest(refreshToken, deviceCode);
        authService.logoutUser(validRequest);
        CookieUtil.expiredAuthCookie(response);
        return BaseResponse.ok(null);
    }
}
