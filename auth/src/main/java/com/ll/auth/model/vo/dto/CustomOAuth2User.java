package com.ll.auth.model.vo.dto;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public record CustomOAuth2User(
        Map<String, Object> attributes,
        Map<String, Object> claims,           // OIDC용
        OidcUserInfo userInfoDetails              // OIDC용
) implements OAuth2User, OidcUser {  // 둘 다 구현!

    @Override public String getName() { return null; }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("USER"));
    }

    // OAuth2User
    @Override public Map<String, Object> getAttributes() { return attributes; }

    // OidcUser
    @Override public Map<String, Object> getClaims() { return claims; }
    @Override public OidcUserInfo getUserInfo() { return new OidcUserInfo(attributes); }
    @Override public OidcIdToken getIdToken() { return null; } // 필요 없으면 null
}