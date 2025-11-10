package com.example.user.service;

import com.example.user.model.vo.request.UserLoginRequest;
import com.example.user.model.vo.request.UserPatchRequest;
import com.example.user.model.vo.response.UserLoginResponse;
import com.example.user.model.vo.response.UserPatchResponse;
import com.example.user.model.vo.response.UserResponse;

import java.util.List;

public interface UserService {

    UserResponse getUserById(Long id);
    UserPatchResponse updateUser(UserPatchRequest request , Long userId);
    List<UserResponse> getUserList();
    UserLoginResponse createOrUpdateUser(UserLoginRequest request);
}
