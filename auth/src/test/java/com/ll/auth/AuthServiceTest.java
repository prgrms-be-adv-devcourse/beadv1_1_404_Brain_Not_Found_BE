package com.ll.auth;

import com.ll.auth.exception.DeviceCodeNotProvidedException;
import com.ll.auth.exception.TokenNotFoundException;
import com.ll.auth.exception.TokenNotProvidedException;
import com.ll.auth.model.entity.Auth;
import com.ll.auth.model.vo.dto.Tokens;
import com.ll.auth.model.vo.request.TokenValidRequest;
import com.ll.auth.oAuth2.JWTProvider;
import com.ll.auth.repository.AuthRepository;
import com.ll.auth.service.AuthAsyncService;
import com.ll.auth.service.AuthService;
import com.ll.auth.service.RedisService;
import com.ll.common.model.enums.Role;
import com.ll.user.model.vo.response.UserResponse;
import com.ll.user.service.UserService;
import io.lettuce.core.RedisConnectionException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.*;

    @ExtendWith(MockitoExtension.class)
    class AuthServiceTest {

    @Mock private AuthRepository authRepository;
    @Mock private RedisService redisService;
    @Mock private UserService userService;
    @Mock private JWTProvider jwtProvider;
    @Mock private AuthAsyncService authAsyncService;
    @InjectMocks private AuthService authService;

    private static final String USER_CODE = "USER_001";
    private static final String DEVICE_CODE = "DEVICE_001";
    private static final String EXIST_REFRESH = "exist-refresh-token";
    private static final String NEW_ACCESS = "new-access-token";
    private static final String NEW_REFRESH = "new-refresh-token";
    private static final String NON_EXIST_REFRESH = "non-exist-refresh-token";

    @Nested
    @DisplayName("refreshToken() 테스트")
    class RefreshTokenTest {

        private UserResponse mockUser() {
            UserResponse user = mock(UserResponse.class);
            given(user.code()).willReturn(USER_CODE);
            given(user.role()).willReturn(Role.USER);
            return user;
        }
        @Test
        @DisplayName("refreshToken 값 비어있으면 TokenNotProvidedException 예외")
        void refreshTokenNotProvidedToken() {
            //given
            TokenValidRequest request = new TokenValidRequest(null,DEVICE_CODE);
            // when & then
            assertThrows(TokenNotProvidedException.class, () -> {
                authService.refreshToken(request);
            });
        }
        
        @Test
        @DisplayName("deviceCode 값 비어있으면 DeviceCOdeNotProvidedException 예외")
        void deviceCodeNotProvidedToken() {
            //given
            TokenValidRequest request = new TokenValidRequest(EXIST_REFRESH,null);

            //when & then
            assertThrows(DeviceCodeNotProvidedException.class, () -> {
                authService.refreshToken(request);
            });
        }

        @Test
        @DisplayName("Redis에 값이 존재한다면 정상적으로 토큰 재발급")
        void refreshToken_success_byRedis() {
            // given
            TokenValidRequest request = new TokenValidRequest(EXIST_REFRESH,DEVICE_CODE);

            given(redisService.validRefreshToken(request.refreshToken(),request.deviceCode()))
                    .willReturn(true);

            given(redisService.getUserCode(request.refreshToken(),request.deviceCode()))
                    .willReturn(USER_CODE);

            UserResponse user = mockUser();

            given(userService.getUserByUserCode(USER_CODE))
                    .willReturn(user);

            Tokens tokens = new Tokens(NEW_ACCESS,NEW_REFRESH);
            given(jwtProvider.createToken(user.code(),user.role().name()))
                    .willReturn(tokens);

            //when
            Tokens result = authService.refreshToken(request);

            //then
            assertThat(result).isNotNull();
            assertThat(result.accessToken()).isEqualTo(NEW_ACCESS);
            assertThat(result.refreshToken()).isEqualTo(NEW_REFRESH);
            verify(authAsyncService).asyncUpsert(USER_CODE, DEVICE_CODE, NEW_REFRESH);
            verify(redisService).saveRefreshToken(USER_CODE,DEVICE_CODE,NEW_REFRESH);
        }

        @Test
        @DisplayName("Redis에 값이없을때 DB 조회 후 갱신해서 값 재확인 후 정상적으로 토큰 발급")
        void refreshToken_success_byDB(){
            // given
            TokenValidRequest request = new TokenValidRequest(EXIST_REFRESH,DEVICE_CODE);

            given(redisService.validRefreshToken(request.refreshToken(),request.deviceCode()))
                    .willReturn(false)
                    .willReturn(true);

            Auth auth = mock(Auth.class);
            given(auth.getUserCode()).willReturn(USER_CODE);

            given(authRepository.findByRefreshToken(request.refreshToken()))
                    .willReturn(Optional.of(auth));

            UserResponse user = mockUser();

            given(userService.getUserByUserCode(USER_CODE))
                    .willReturn(user);

            Tokens tokens = new Tokens(NEW_ACCESS,NEW_REFRESH);
            given(jwtProvider.createToken(user.code(),user.role().name()))
                    .willReturn(tokens);

            //when
            Tokens result = authService.refreshToken(request);

            //then
            assertThat(result).isNotNull();
            assertThat(result.accessToken()).isEqualTo(NEW_ACCESS);
            assertThat(result.refreshToken()).isEqualTo(NEW_REFRESH);
            verify(authAsyncService).asyncUpsert(USER_CODE, DEVICE_CODE, NEW_REFRESH);
            verify(redisService).saveRefreshToken(USER_CODE,DEVICE_CODE,NEW_REFRESH);
        }

        @Test
        @DisplayName("RedisServer가 꺼져있어도 DB에 값이 존재한다면 정상적으로 토큰 재발급")
        void refreshToken_success_redisDown() {

            // given
            TokenValidRequest request = new TokenValidRequest(EXIST_REFRESH,DEVICE_CODE);

            given(redisService.validRefreshToken(request.refreshToken(),request.deviceCode()))
                    .willThrow(RedisConnectionException.class);

            Auth auth = mock(Auth.class);
            given(auth.getUserCode()).willReturn(USER_CODE);

            given(authRepository.findByRefreshToken(request.refreshToken()))
                    .willReturn(Optional.of(auth));

            UserResponse user = mockUser();

            given(userService.getUserByUserCode(USER_CODE))
                    .willReturn(user);

            Tokens tokens = new Tokens(NEW_ACCESS,NEW_REFRESH);
            given(jwtProvider.createToken(user.code(),user.role().name()))
                    .willReturn(tokens);

            //when
            Tokens result = authService.refreshToken(request);

            //then
            assertThat(result).isNotNull();
            assertThat(result.accessToken()).isEqualTo(NEW_ACCESS);
            assertThat(result.refreshToken()).isEqualTo(NEW_REFRESH);
            verify(authAsyncService).asyncUpsert(USER_CODE, DEVICE_CODE, NEW_REFRESH);
        }

        @Test
        @DisplayName("Redis와 DB 모두에 토큰이 없으면 TokenNotFoundException 발생")
        void refreshToken_notFound() {
            // given
            TokenValidRequest request = new TokenValidRequest(NON_EXIST_REFRESH, DEVICE_CODE);

            // Redis에 값 없음
            given(redisService.validRefreshToken(request.refreshToken(), request.deviceCode()))
                    .willReturn(false);

            // DB에도 값 없음
            given(authRepository.findByRefreshToken(request.refreshToken()))
                    .willReturn(Optional.empty());

            // when & then
            assertThrows(TokenNotFoundException.class, () -> authService.refreshToken(request));
        }

    }
}