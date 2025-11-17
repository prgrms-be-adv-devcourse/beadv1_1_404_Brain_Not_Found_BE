package com.ll.auth.controller;
import com.ll.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

//    @PostMapping
//    public ResponseEntity<BaseResponse<Tokens>> refreshToken(
//            @RequestBody String refreshToken,
//            @RequestHeader(value="X-User-Code") String userCode
//    ){
//        TokenValidRequest request = new TokenValidRequest(userCode, refreshToken);
//        return BaseResponse.ok(authService.refreshToken(request));
//    }
}
