package com.ll.auth.service;

import com.ll.auth.exception.TokenNotFoundException;
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

    public void save(Auth auth){
        authRepository.save(auth);
    }

    public Tokens refreshToken(TokenValidRequest request){

        Auth existAuth = authRepository.findByUserCode(request.userCode()).orElseThrow(TokenNotFoundException::new);
        String existRefreshToken = existAuth.getRefreshToken();

        if(!existRefreshToken.trim().equals(request.refreshToken().trim())){
            throw new TokenNotFoundException();
        }
        else{
            Tokens tokens = jWTProvider.createToken(request.userCode(),request.role());
            existAuth.updateRefreshToken(tokens.refreshToken());
            authRepository.save(existAuth);
            return tokens;
        }
    }

    public void logoutUser(String userCode , HttpServletResponse response){
        ResponseCookie accessTokenCookie = CookieUtil.expiredCookie("accessToken");
        ResponseCookie refreshTokenCookie = CookieUtil.expiredCookie("refreshToken");
        response.addHeader(HttpHeaders.SET_COOKIE,accessTokenCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE,refreshTokenCookie.toString());
        authRepository.deleteByUserCode(userCode);
    }
    public Optional<Auth> getAuthByUserCode(String userCode){
        return authRepository.findByUserCode(userCode);
    }

}
