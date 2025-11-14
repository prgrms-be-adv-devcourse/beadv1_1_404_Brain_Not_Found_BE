package com.ll.user.controller;


import com.example.core.exception.ErrorCode;
import com.ll.user.model.vo.request.UserPatchRequest;
import com.ll.user.model.vo.request.UserLoginRequest;
import com.ll.user.model.vo.response.UserLoginResponse;
import com.ll.user.model.vo.response.UserResponse;
import com.ll.user.service.UserService;
import com.example.core.model.response.BaseResponse;
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
    public ResponseEntity<BaseResponse<UserResponse>> getUser(
            @RequestHeader(value = "X-User-Code", required = false) String userCode,
            @RequestParam(value = "id", required = false) Long id) {
        try {
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
    public ResponseEntity<BaseResponse<UserResponse>> updateUser(
            @RequestBody UserPatchRequest request,
            @RequestHeader(value = "X-User-Code", required = true) String userCode
    ){
        try {
            UserResponse updatedUser = userService.updateUser(request, userCode);
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
