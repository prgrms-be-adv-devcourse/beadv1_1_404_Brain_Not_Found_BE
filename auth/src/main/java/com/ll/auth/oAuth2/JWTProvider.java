package com.ll.auth.oAuth2;

import com.ll.auth.model.vo.dto.Tokens;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Component
public class JWTProvider {

    @Value("${jwt.secret:your-default-secret-key-must-be-at-least-256-bits-long}")  // application.yml 설정
    private String secretKey;

    @Value("${jwt.expiration:1800000}")  // 30분 (밀리초)
    public
    Long expirationTime;

    @Value("${jwt.refresh-expiration:604800000}")  // 7일 = 7 * 24 * 60 * 60 * 1000
    public Long refreshExpirationTime;

    private SecretKey key;  // HS512 서명 키

    @PostConstruct  // 빈 초기화 시 키 생성
    public void init() {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    public Tokens createToken(String userCode , String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userCode", userCode);
        claims.put("role",role);

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationTime);
        Date refreshExpirationDate = new Date(now.getTime() + refreshExpirationTime);
        String accessToken = Jwts.builder()
                .claims(claims)  // 커스텀 클레임// 사용자 ID
                .issuedAt(now)
                .issuer("Gooream")  // 앱 식별자
                .expiration(expiryDate)
                .signWith(key)  // 서명
                .compact();
        String refreshToken = Jwts.builder()
                .claims(claims)  // 커스텀 클레임// 사용자 ID
                .issuedAt(now)
                .issuer("Gooream")  // 앱 식별자
                .expiration(refreshExpirationDate)
                .signWith(key)  // 서명
                .compact();

        return new Tokens(accessToken, refreshToken);
    }


    /**
     * 토큰 유효성 검사
     *
     * @param token JWT 토큰
     * @return 유효 시 true
     */
    public boolean validateToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            Date expiration = claims.getExpiration();
            return expiration == null || !expiration.before(new Date());  // 만료됨
        } catch (Exception e) {
            return false;
        }
    }

    public Authentication getAuthentication(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        String userCode = claims.get("userCode",String.class);
        String role = claims.get("role", String.class);
        Collection<GrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + role)
        );

        return new UsernamePasswordAuthenticationToken(userCode, token, authorities);
    }
}
