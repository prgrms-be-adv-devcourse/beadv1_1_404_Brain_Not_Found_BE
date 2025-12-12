package com.ll.user.controller;

import com.ll.auth.model.vo.dto.Tokens;
import com.ll.auth.service.AuthService;
import com.ll.auth.util.CookieUtil;
import com.ll.user.model.vo.request.UserPatchRequest;

import com.ll.user.model.vo.response.UserResponse;
import com.ll.user.service.UserService;
import com.ll.core.model.response.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
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
    @Operation(summary = "01. 회원 목록 조회" , description = "전체 회원 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<BaseResponse<List<UserResponse>>> getAllUsers() {
        List<UserResponse> users = userService.getUserList();
        return BaseResponse.ok(users);
    }

    // 회원 정보 조회
    @Operation(summary = "02. 회원 정보 조회" , description = "UserCode로 회원 정보를 조회합니다.")
    @GetMapping("/info")
    public ResponseEntity<BaseResponse<UserResponse>> getUser(
            @Parameter(hidden = true)
            @RequestHeader(value = "X-User-Code") String userCode
    ) {
            return BaseResponse.ok(userService.getUserByUserCode(userCode));
    }

    // 회원 정보 수정
    @Operation(summary = "03. 회원 정보 수정" ,
            description =
                    """
                            UserPatchRequest를 기반으로 회원정보를 수정합니다.
                            수정한 유저 정보 기반으로 토큰을 재발급합니다.
                            Cookie의 AccessToken을 Gateway가 검증하여 userCode를 추출합니다.
                            Cookie의 deviceCode와 userCode를 사용해 토큰을 저장합니다.""")
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
