package com.ll.user.model.entity;

import com.github.f4b6a3.uuid.UuidCreator;
import com.ll.user.model.enums.AccountStatus;
import com.ll.user.model.enums.Grade;
import com.ll.user.model.enums.Role;
import com.ll.user.model.enums.SocialProvider;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String socialId;

    @Column(nullable = false)
    private String userCode;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private SocialProvider socialProvider;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Role role = Role.USER;


    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

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


    public void updateSocialInfo(String socialId, SocialProvider socialProvider, String email, String name) {
        this.socialId = socialId;
        this.socialProvider = socialProvider;
        this.email = email;
        this.name = name;
    }

    public void generateUserCode(){
        this.userCode = "USER-" + UuidCreator.getTimeOrderedEpoch().toString().substring(0, 8).toUpperCase();
    }


}
