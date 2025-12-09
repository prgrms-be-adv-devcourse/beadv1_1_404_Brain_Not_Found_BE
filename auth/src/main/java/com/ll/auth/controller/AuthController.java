package com.ll.auth.controller;
import com.ll.auth.util.CookieUtil;
import com.ll.core.model.response.BaseResponse;
import com.ll.auth.model.vo.dto.Tokens;
import com.ll.auth.model.vo.request.TokenValidRequest;
import com.ll.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name ="Auth", description = "토큰 관리 API")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping
    @Operation(summary = "토큰 재발급" ,
            description =
                    "Cookie 에 저장된 refreshToken, deviceCode 값을 통해 토큰을 재발급하여 쿠키에 저장합니다.")
    public ResponseEntity<BaseResponse<Tokens>> refreshToken(
            @CookieValue(name = "refreshToken", required = false) String refreshToken,
            @CookieValue(name = "deviceCode", required = false) String deviceCode,
            HttpServletResponse response
    ){
        TokenValidRequest validRequest = new TokenValidRequest(refreshToken,deviceCode);
        Tokens tokens = authService.refreshToken(validRequest);
        CookieUtil.setTokenCookie(response, tokens.accessToken(),  tokens.refreshToken());
        return BaseResponse.ok(tokens);
    }

    @PostMapping("/logout")
    @Operation(summary = "로그아웃" , description =
            "Cookie 에 저장된 refreshToken, deviceCode 값을 통해 토큰 정보를 Redis 및 DB에서 삭제하고, 쿠키에서 만료시킵니다.")

    public ResponseEntity<Void> logout(
            @CookieValue(name = "refreshToken") String refreshToken,
            @CookieValue(name ="deviceCode", required = false) String deviceCode,
            HttpServletResponse response
    ){
        authService.logoutUser(refreshToken,deviceCode);
        CookieUtil.expiredAuthCookie(response);
        return ResponseEntity.ok().build();
    }
}
