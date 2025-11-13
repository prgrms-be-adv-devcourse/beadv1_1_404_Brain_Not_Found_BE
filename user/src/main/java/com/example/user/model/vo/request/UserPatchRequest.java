package com.example.user.model.vo.request;


import com.example.user.model.enums.Grade;

public record UserPatchRequest(
        String name,
        String profileImageUrl,
        String accountBank,
        String accountNumber,
        String address,
        Grade grade,
        Long mannerScore
) {


}
