package com.ll.user.controller;

import com.ll.auth.model.vo.dto.Tokens;
import com.ll.auth.service.AuthService;
import com.ll.auth.util.CookieUtil;
import com.ll.user.model.vo.request.UserPatchRequest;
import com.ll.common.model.vo.request.UserLoginRequest;
import com.ll.common.model.vo.response.UserLoginResponse;
import com.ll.user.model.vo.response.UserResponse;
import com.ll.user.service.UserService;
import com.ll.core.model.response.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Tag(name = "User", description = "사용자 관련 API")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final AuthService authService;

    // 회원 정보 조회
    @Operation(summary = "회원 정보 조회" , description = "UserCode로 회원 정보를 조회합니다.")
    @GetMapping("/info")
    public ResponseEntity<BaseResponse<UserResponse>> getUser(
            @RequestHeader(value = "X-User-Code") String userCode
    ) {
            return BaseResponse.ok(userService.getUserByUserCode(userCode));
    }

    // 소셜로그인
    @Operation(summary = "소셜로그인" , description = "소셜로그인 정보 기반으로 회원을 Upsert 합니다.")
    @PostMapping
    public ResponseEntity<BaseResponse<UserLoginResponse>> socialLogin(@RequestBody @Validated UserLoginRequest request) {
        UserLoginResponse response = userService.createOrUpdateUser(request);
        return BaseResponse.ok(response);
    }

    // 회원 정보 수정
    @Operation(summary = "회원 정보 수정" ,
            description =
                    "UserPatchRequest를 기반으로 회원정보를 수정합니다. 수정한 유저 정보 기반으로 토큰을 재발급합니다.")
    @PatchMapping
    public ResponseEntity<BaseResponse<UserResponse>> updateUser(
            @RequestBody @Validated UserPatchRequest request,
            @RequestHeader(value = "X-User-Code") String userCode,
            @CookieValue(value = "deviceCode" , required = false) String deviceCode,
            HttpServletResponse response
    ){
            UserResponse user = userService.updateUser(request,userCode);
            Tokens tokens = authService.issuedToken(userCode,deviceCode, user.role().name());
            CookieUtil.setTokenCookie(response,tokens.accessToken(),tokens.refreshToken());
            return BaseResponse.ok(user);
    }

    // 회원 목록 조회
    @Operation(summary = "회원 목록 조회" , description = "전체 회원 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<BaseResponse<List<UserResponse>>> getAllUsers() {
        List<UserResponse> users = userService.getUserList();
        return BaseResponse.ok(users);
    }
}
