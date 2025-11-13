package com.example.user.model.vo.response;

import java.util.List;

public record UserListResponse(
        List<UserResponse> userList
) {
}
