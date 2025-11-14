package com.ll.auth.model.entity;


import com.example.core.model.persistence.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class Auth extends BaseEntity {

    @Column(nullable = false)
    private String userCode;

    @Column(nullable = false)
    private String refreshToken;
}
