package com.ll.user.controller;

import com.ll.auth.service.AuthService;
import com.ll.user.model.vo.request.UserPatchRequest;
import com.ll.common.model.vo.request.UserLoginRequest;
import com.ll.common.model.vo.response.UserLoginResponse;
import com.ll.user.model.vo.response.UserResponse;
import com.ll.user.service.UserService;
import com.ll.core.model.response.BaseResponse;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final AuthService authService;

    // 회원 정보 조회
    @GetMapping("/info")
    public ResponseEntity<BaseResponse<UserResponse>> getUser(
            @RequestHeader(value = "X-User-Code") String userCode
    ) {
            return BaseResponse.ok(userService.getUserByUserCode(userCode));
    }

    // 소셜로그인
    @PostMapping
    public ResponseEntity<BaseResponse<UserLoginResponse>> socialLogin(@RequestBody @Validated UserLoginRequest request) {
        UserLoginResponse response = userService.createOrUpdateUser(request);
        return BaseResponse.ok(response);
    }

    // 회원 정보 수정
    @PatchMapping
    public ResponseEntity<BaseResponse<UserResponse>> updateUser(
            @RequestBody @Validated UserPatchRequest request,
            @RequestHeader(value = "X-User-Code") String userCode,
            @CookieValue(value = "deviceCode") String deviceCode,
            HttpServletResponse response
    ){
            UserResponse user = userService.updateUser(request,userCode);
            authService.issuedToken(userCode,deviceCode, user.role().name(),response);
            return BaseResponse.ok(user);
    }

    // 회원 목록 조회
    @GetMapping
    public ResponseEntity<BaseResponse<List<UserResponse>>> getAllUsers() {
        List<UserResponse> users = userService.getUserList();
        return BaseResponse.ok(users);
    }
}
