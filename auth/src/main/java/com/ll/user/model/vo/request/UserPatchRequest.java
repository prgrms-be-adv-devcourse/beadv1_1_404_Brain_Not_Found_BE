package com.ll.user.model.vo.request;


import com.ll.common.model.enums.Grade;
import com.ll.common.model.enums.Role;
import jakarta.validation.constraints.Email;

public record UserPatchRequest(
        String name,
        String profileImageUrl,
        @Email
        String email,
        String accountBank,
        String accountNumber,
        String address,
        Role role,
        Grade grade,
        Long mannerScore
) {

}
