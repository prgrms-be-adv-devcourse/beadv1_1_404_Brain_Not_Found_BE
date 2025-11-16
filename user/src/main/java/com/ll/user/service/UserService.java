package com.ll.user.service;

import com.ll.user.model.vo.request.UserLoginRequest;
import com.ll.user.model.vo.request.UserPatchRequest;
import com.ll.user.model.vo.response.UserLoginResponse;
import com.ll.user.model.vo.response.UserPatchResponse;
import com.ll.user.model.vo.response.UserResponse;

import java.util.List;

public interface UserService {

    UserResponse getUserById(Long id);
    UserPatchResponse updateUser(UserPatchRequest request , Long userId);
    List<UserResponse> getUserList();
    UserLoginResponse createOrUpdateUser(UserLoginRequest request);
}
