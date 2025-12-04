package com.ll.auth;

import com.ll.auth.exception.TokenNotFoundException;
import com.ll.auth.model.entity.Auth;
import com.ll.auth.model.vo.dto.Tokens;
import com.ll.auth.model.vo.request.TokenValidRequest;
import com.ll.auth.oAuth2.JWTProvider;
import com.ll.auth.repository.AuthRepository;
import com.ll.auth.service.AuthAsyncService;
import com.ll.auth.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.*;

    @ExtendWith(MockitoExtension.class)
    class AuthServiceTest {

    @Mock private AuthRepository authRepository;
    @Mock private JWTProvider jWTProvider;
    @InjectMocks private AuthService authService;
    @InjectMocks private AuthAsyncService authAsyncService;

    private static final String USER_CODE = "USER_001";
    private static final String DEVICE_CODE = "DEVICE_001";
    private static final String ROLE = "ROLE_USER";
    private static final String EXIST_REFRESH = "exist-refresh-token";
    private static final String NEW_ACCESS = "new-access-token";
    private static final String NEW_REFRESH = "new-refresh-token";

    @Test
    @DisplayName("save() - Auth 엔티티를 repository에 저장한다")
    void save() {
        // given
        Auth auth = Auth.builder()
                .userCode(USER_CODE)
                .refreshToken(EXIST_REFRESH)
                .build();

        // when
        authAsyncService.asyncUpsert(auth.getUserCode(),auth.getDeviceCode(),auth.getRefreshToken());

        // then
        then(authRepository).should().save(auth);
    }

    @Nested
    @DisplayName("refreshToken() 테스트")
    class RefreshTokenTest {

        private TokenValidRequest validRequest() {
            return new TokenValidRequest(EXIST_REFRESH,DEVICE_CODE );
        }

        private Tokens newTokens() {
            return new Tokens(NEW_ACCESS, NEW_REFRESH);
        }

        @Test
        @DisplayName("정상 흐름 - 기존 토큰이 일치하면 새 토큰을 발급하고 DB를 갱신한다")
        void refreshToken_success() {
            // given
            Auth existingAuth = Auth.builder()
                    .userCode(USER_CODE)
                    .refreshToken(EXIST_REFRESH)
                    .build();

            given(authRepository.findByUserCode(USER_CODE))
                    .willReturn(Optional.of(existingAuth));

            Tokens newTokens = new Tokens(NEW_ACCESS, NEW_REFRESH);
            given(jWTProvider.createToken(USER_CODE, ROLE))
                    .willReturn(newTokens);

            TokenValidRequest request = validRequest(); // 기존에 만든 fixture

            // when
            Tokens result = authService.refreshToken(request);

            // then
            assertThat(result.accessToken()).isEqualTo(NEW_ACCESS);
            assertThat(result.refreshToken()).isEqualTo(NEW_REFRESH);

            then(authRepository).should(times(1)).save(any(Auth.class));

            then(authRepository).should().save(argThat(saved ->
                    saved.getUserCode().equals(USER_CODE) &&
                            saved.getRefreshToken().equals(NEW_REFRESH)
            ));

            then(authRepository).shouldHaveNoMoreInteractions();
        }

        @Test
        @DisplayName("기존 Auth 가 없을 때 TokenNotFoundException 발생")
        void refreshToken_noExistingAuth_throwTokenNotFoundException() {
            // given
            given(authRepository.findByUserCode(USER_CODE))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> authService.refreshToken(validRequest()))
                    .isInstanceOf(TokenNotFoundException.class);
        }

        @Test
        @DisplayName("제공된 refreshToken 이 다를 때 TokenNotFoundException 발생")
        void refreshToken_mismatchRefreshToken_throwTokenNotFoundException() {
            // given
            Auth authWithDifferentToken = Auth.builder()
                    .userCode(USER_CODE)
                    .refreshToken("different-token")
                    .build();

            given(authRepository.findByUserCode(USER_CODE))
                    .willReturn(Optional.of(authWithDifferentToken));

            TokenValidRequest request = new TokenValidRequest(EXIST_REFRESH,DEVICE_CODE);

            // when & then
            assertThatThrownBy(() -> authService.refreshToken(request))
                    .isInstanceOf(TokenNotFoundException.class);
        }
    }
}