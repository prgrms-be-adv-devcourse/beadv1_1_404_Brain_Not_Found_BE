package com.ll.user.controller;


import com.ll.core.model.exception.ErrorCode;
import com.ll.user.model.vo.request.UserPatchRequest;
import com.ll.user.model.vo.request.UserLoginRequest;
import com.ll.user.model.vo.response.UserLoginResponse;
import com.ll.user.model.vo.response.UserPatchResponse;
import com.ll.user.model.vo.response.UserResponse;
import com.ll.user.service.UserService;
import com.ll.core.model.response.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    // 회원 정보 조회
    @GetMapping("/info")
    public ResponseEntity<BaseResponse<UserResponse>> getUser(@RequestParam Long id) {
        try {
            UserResponse user = userService.getUserById(id);
            return BaseResponse.ok(user);
        } catch (NoSuchElementException e) {
            return BaseResponse.error(ErrorCode.NOT_FOUND,"유저를 찾을 수 없습니다.");
        }

    }

    // 소셜로그인
    @PostMapping
    public ResponseEntity<BaseResponse<UserLoginResponse>> socialLogin(@RequestBody UserLoginRequest request) {
        UserLoginResponse response = userService.createOrUpdateUser(request);
        return BaseResponse.ok(response);
    }

    // 회원 정보 수정
    @PatchMapping
    public ResponseEntity<BaseResponse<UserPatchResponse>> updateUser(@RequestBody UserPatchRequest request, @RequestParam Long id) {
        try {
            UserPatchResponse updatedUser = userService.updateUser(request, id);
            return BaseResponse.ok(updatedUser);
        } catch (NoSuchElementException e) {
            return BaseResponse.error(ErrorCode.NOT_FOUND);
        } catch (Exception e) {
            return BaseResponse.error(ErrorCode.BAD_REQUEST);
        }
    }

    // 회원 목록 조회
    @GetMapping
    public ResponseEntity<BaseResponse<List<UserResponse>>> getAllUsers() {
        List<UserResponse> users = userService.getUserList();
        return BaseResponse.ok(users);
    }
}
