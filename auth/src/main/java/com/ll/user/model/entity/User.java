package com.ll.user.model.entity;

import com.ll.core.model.persistence.BaseEntity;
import com.ll.common.model.enums.AccountStatus;
import com.ll.common.model.enums.Grade;
import com.ll.common.model.enums.Role;
import com.ll.common.model.enums.SocialProvider;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.*;


@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "users")
public class User extends BaseEntity {

    @Column(nullable = false)
    private String socialId;

    @Column(nullable = false)
    @Email
    private String email;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private SocialProvider socialProvider = SocialProvider.KAKAO; // 기본값 예시

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Role role = Role.USER;

    private String profileImageUrl;

    @Column(nullable = false)
    @Builder.Default
    private Long mannerScore = 5L;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Grade grade = Grade.BRONZE;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private AccountStatus accountStatus = AccountStatus.ACTIVE;

    private String accountBank;
    private String accountNumber;
    private String address;

    public void updateSocialInfo(String email, String name) {
        this.email = email;
        this.name = name;
    }


}
