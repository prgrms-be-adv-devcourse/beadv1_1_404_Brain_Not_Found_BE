package com.ll.auth.controller;
import com.ll.auth.service.RedisService;
import com.ll.auth.util.CookieUtil;
import com.ll.core.model.response.BaseResponse;
import com.ll.auth.model.vo.dto.Tokens;
import com.ll.auth.model.vo.request.TokenValidRequest;
import com.ll.auth.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final RedisService redisService;

    @PostMapping
    public ResponseEntity<Void> refreshToken(
            @CookieValue(name = "refreshToken", required = false) String refreshToken,
            @CookieValue(name = "deviceCode", required = false) String deviceCode,
            @RequestHeader(value="X-User-Code") String userCode,
            @RequestHeader(value="X-Role") String role,
            HttpServletResponse response
    ){
        TokenValidRequest validRequest = new TokenValidRequest(userCode,role,refreshToken,deviceCode);
        authService.refreshToken(validRequest,response);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @RequestHeader(value="X-User-Code") String userCode,
            @CookieValue(name ="deviceCode", required = false) String deviceCode,
            HttpServletResponse response
    ){
        authService.logoutUser(userCode,response,deviceCode);
        return ResponseEntity.ok().build();
    }
}
