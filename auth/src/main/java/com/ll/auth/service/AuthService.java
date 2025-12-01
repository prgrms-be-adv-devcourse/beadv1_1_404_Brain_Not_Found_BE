package com.ll.auth.service;

import com.ll.auth.exception.DeviceCodeNotProvidedException;
import com.ll.auth.exception.TokenNotFoundException;
import com.ll.auth.exception.TokenNotProvidedException;
import com.ll.auth.model.entity.Auth;
import com.ll.auth.model.vo.dto.Tokens;
import com.ll.auth.model.vo.request.TokenValidRequest;
import com.ll.auth.oAuth2.JWTProvider;
import com.ll.auth.repository.AuthRepository;
import com.ll.auth.util.CookieUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthRepository authRepository;
    private final JWTProvider jWTProvider;
    private final RedisService redisService;

    public void save(Auth auth){
        authRepository.save(auth);
    }

    public Tokens refreshToken(TokenValidRequest request){

        if(request.refreshToken() == null || request.refreshToken().isEmpty()){
            throw new TokenNotProvidedException();
        }
        if(request.deviceCode() == null || request.deviceCode().isEmpty()){
            throw new DeviceCodeNotProvidedException();
        }
        if(redisService.getRefreshToken(request.userCode(),request.deviceCode()).equals(request.refreshToken())){
            Tokens tokens = jWTProvider.createToken(request.userCode(),request.role());
            redisService.saveRefreshToken(request.userCode(),request.deviceCode(),tokens.refreshToken());
            return tokens;
        }
        else{
            throw new TokenNotFoundException();
        }
    }

    public void logoutUser(String userCode , HttpServletResponse response , String deviceCode){

        redisService.deleteRefreshToken(userCode,deviceCode);
        ResponseCookie accessTokenCookie = CookieUtil.expiredCookie("accessToken");
        ResponseCookie refreshTokenCookie = CookieUtil.expiredCookie("refreshToken");
        response.addHeader(HttpHeaders.SET_COOKIE,accessTokenCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE,refreshTokenCookie.toString());


    }

}
