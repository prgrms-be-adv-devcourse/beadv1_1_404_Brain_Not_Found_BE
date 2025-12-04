package com.ll.user.service;

import com.ll.common.model.vo.request.UserLoginRequest;
import com.ll.user.model.vo.request.UserPatchRequest;
import com.ll.common.model.vo.response.UserLoginResponse;
import com.ll.user.model.vo.response.UserResponse;

import java.util.List;

public interface UserService {

    UserResponse getUserById(Long id);
    UserResponse updateUser(UserPatchRequest request, String userCode);
    List<UserResponse> getUserList();
    UserLoginResponse createOrUpdateUser(UserLoginRequest request);
    UserResponse getUserByUserCode(String userCode);
}
