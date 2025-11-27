package com.ll.auth.oAuth2;

import com.ll.common.model.enums.SocialProvider;
import com.ll.common.model.vo.request.UserLoginRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2UserFactory {

    public UserLoginRequest getOAuth2UserInfo(Authentication authentication) {
        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        String registrationId = oauthToken.getAuthorizedClientRegistrationId();

        if (registrationId.equals("google")) {
            return extractGoogleUserInfo((DefaultOidcUser) authentication.getPrincipal());
        } else if (registrationId.equals("naver")) {
            return extractNaverUserInfo((DefaultOAuth2User) authentication.getPrincipal());
        }
        throw new OAuth2AuthenticationException("Unsupported OAuth2 provider");
    }

    private UserLoginRequest extractGoogleUserInfo(DefaultOAuth2User oauth2User) {
        Map<String, Object> attributes = oauth2User.getAttributes();

        return UserLoginRequest.builder()
                .socialId((String) attributes.get("sub"))
                .email((String) attributes.get("email"))
                .name((String) attributes.get("name"))
                .socialProvider(SocialProvider.GOOGLE)
                .build();
    }

    private UserLoginRequest extractNaverUserInfo(DefaultOAuth2User oAuth2User) {
        Map<String, Object> attributes = oAuth2User.getAttributes();
        Map<String, Object> response = (Map<String, Object>) attributes.get("response");

        return UserLoginRequest.builder()
                .socialId((String) response.get("id"))
                .email((String) response.get("email"))
                .name((String) response.get("name"))
                .socialProvider(SocialProvider.NAVER)
                .build();
    }

}