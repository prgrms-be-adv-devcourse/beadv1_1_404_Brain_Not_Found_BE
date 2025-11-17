package com.ll.auth.controller;
import com.example.core.model.response.BaseResponse;
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
            @RequestBody String refreshToken,
            @RequestHeader(value="X-User-Code") String userCode,
            @RequestHeader(value="X-Role") String role
    ){
        TokenValidRequest request = new TokenValidRequest(userCode,role, refreshToken);
        return BaseResponse.ok(authService.refreshToken(request));
    }
}
