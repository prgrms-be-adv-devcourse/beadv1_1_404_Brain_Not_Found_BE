package com.ll.auth.model.entity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class Auth {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String refreshToken;

    @Column(nullable = false)
    private String userCode;

    @Column(nullable = false)
    private String deviceCode;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime expiredAt = LocalDateTime.now().plusDays(7);



    public void updateRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
        this.expiredAt = LocalDateTime.now().plusDays(7);
    }
}
