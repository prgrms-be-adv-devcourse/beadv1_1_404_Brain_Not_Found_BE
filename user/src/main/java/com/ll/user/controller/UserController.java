package com.ll.user.controller;

import com.ll.core.model.exception.ErrorCode;
import com.ll.user.model.vo.request.UserPatchRequest;
import com.ll.user.model.vo.request.UserLoginRequest;
import com.ll.user.model.vo.response.UserLoginResponse;
import com.ll.user.model.vo.response.UserResponse;
import com.ll.user.service.UserService;
import com.ll.core.model.response.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/ping")
    public ResponseEntity<BaseResponse<String>> pong() {
        System.out.println("ProductController.pong");
        return BaseResponse.ok("Ok");
    }

    // 회원 정보 조회
    @GetMapping("/info")
    public ResponseEntity<BaseResponse<UserResponse>> getUser(
            @RequestHeader(value = "X-User-Code", required = false) String userCode,
            @RequestParam(value = "id", required = false) Long id) {
            UserResponse user;
            if (userCode != null && !userCode.isBlank()) {
                user = userService.getUserByUserCode(userCode);
            }
            else if (id != null) {
                user = userService.getUserById(id);
            }
            else {
                return BaseResponse.error(ErrorCode.BAD_REQUEST, "userCode (헤더) 또는 id (쿼리 파라미터) 중 하나가 필요합니다.");
            }
            return BaseResponse.ok(user);

    }

    // 소셜로그인
    @PostMapping
    public ResponseEntity<BaseResponse<UserLoginResponse>> socialLogin(@RequestBody UserLoginRequest request) {
        UserLoginResponse response = userService.createOrUpdateUser(request);
        return BaseResponse.ok(response);
    }

    // 회원 정보 수정
    @PatchMapping
    public ResponseEntity<BaseResponse<UserResponse>> updateUser(
            @RequestBody UserPatchRequest request,
            @RequestHeader(value = "X-User-Code") String userCode
    ){
            return BaseResponse.ok(userService.updateUser(request, userCode));

    }

    // 회원 목록 조회
    @GetMapping
    public ResponseEntity<BaseResponse<List<UserResponse>>> getAllUsers() {
        List<UserResponse> users = userService.getUserList();
        return BaseResponse.ok(users);
    }
}
