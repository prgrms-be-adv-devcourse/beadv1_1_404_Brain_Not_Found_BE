package com.ll.user.controller;

import com.ll.auth.model.vo.dto.Tokens;
import com.ll.auth.service.AuthService;
import com.ll.auth.util.CookieUtil;
import com.ll.user.controller.swagger.UserGetApiResponse;
import com.ll.user.controller.swagger.UserListApiResponse;
import com.ll.user.controller.swagger.UserUpdateApiResponse;
import com.ll.user.model.vo.request.UserPatchRequest;

import com.ll.user.model.vo.response.UserResponse;
import com.ll.user.service.UserService;
import com.ll.core.model.response.BaseResponse;
import io.swagger.v3.oas.annotations.Parameter;
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

    // 회원 목록 조회
    @UserListApiResponse
    @GetMapping
    public ResponseEntity<BaseResponse<List<UserResponse>>> getAllUsers() {
        List<UserResponse> users = userService.getUserList();
        return BaseResponse.ok(users);
    }

    // 회원 정보 조회
    @UserGetApiResponse
    @GetMapping("/info")
    public ResponseEntity<BaseResponse<UserResponse>> getUser(
            @Parameter(hidden = true)
            @RequestHeader(value = "X-User-Code") String userCode
    ) {
            return BaseResponse.ok(userService.getUserByUserCode(userCode));
    }

    // 회원 정보 수정
    @UserUpdateApiResponse
    @PatchMapping
    public ResponseEntity<BaseResponse<UserResponse>> updateUser(
            @RequestBody @Validated UserPatchRequest request,
            @Parameter(hidden = true)
            @RequestHeader(value = "X-User-Code") String userCode,
            @Parameter(hidden = true)
            @CookieValue(value = "deviceCode" , required = false) String deviceCode,
            HttpServletResponse response
    ){
            UserResponse user = userService.updateUser(request,userCode);
            Tokens tokens = authService.issuedToken(userCode,deviceCode, user.role().name());
            CookieUtil.setTokenCookie(response,tokens.accessToken(),tokens.refreshToken());
            return BaseResponse.ok(user);
    }


}
