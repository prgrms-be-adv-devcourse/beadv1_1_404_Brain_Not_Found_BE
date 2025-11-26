package com.ll.user.model.vo.request;


import com.ll.user.model.enums.Grade;
import com.ll.user.model.enums.Role;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
