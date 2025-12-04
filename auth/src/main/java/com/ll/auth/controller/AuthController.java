package com.ll.auth.controller;
import com.ll.auth.util.CookieUtil;
import com.ll.core.model.response.BaseResponse;
import com.ll.auth.model.vo.dto.Tokens;
import com.ll.auth.model.vo.request.TokenValidRequest;
import com.ll.auth.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping
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
    public ResponseEntity<Void> logout(
            @CookieValue(name = "refreshToken") String refreshToken,
            @CookieValue(name ="deviceCode", required = false) String deviceCode,
            HttpServletResponse response
    ){
        authService.logoutUser(refreshToken,deviceCode);
        CookieUtil.expiredCookie(response);
        return ResponseEntity.ok().build();
    }
}
