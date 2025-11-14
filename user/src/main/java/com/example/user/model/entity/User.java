package com.example.user.model.entity;

import com.example.user.model.enums.AccountStatus;
import com.example.user.model.enums.Grade;
import com.example.user.model.enums.Role;
import com.example.user.model.enums.SocialProvider;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

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
    private LocalDate createdAt = LocalDate.now();

    @Column(nullable = false)
    @Builder.Default
    private LocalDate updatedAt = LocalDate.now();

    private String profileImageUrl;

    @Column(nullable = false)
    @Builder.Default
    private Long mannerScore = 5L;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Grade grade = Grade.BRONZE;

    @Column(nullable = false)
    @Builder.Default
    private AccountStatus accountStatus = AccountStatus.ACTIVE;

    private String accountBank;
    private String accountNumber;
    private String address;

}
