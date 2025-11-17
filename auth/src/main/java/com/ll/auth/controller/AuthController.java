package com.ll.auth.controller;
import com.ll.auth.model.vo.dto.RefreshTokenBody;
import com.ll.core.model.response.BaseResponse;
import com.ll.auth.model.vo.dto.Tokens;
import com.ll.auth.model.vo.request.TokenValidRequest;
import com.ll.auth.service.AuthService;
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
            @RequestBody RefreshTokenBody request,
            @RequestHeader(value="X-User-Code") String userCode,
            @RequestHeader(value="X-Role") String role
    ){
        TokenValidRequest validRequest = new TokenValidRequest(userCode,role, request.refreshToken());
        return BaseResponse.ok(authService.refreshToken(validRequest));
    }
}
