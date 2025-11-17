package com.ll.auth.service;

import com.ll.auth.exception.TokenNotFoundException;
import com.ll.auth.model.entity.Auth;
import com.ll.auth.model.vo.dto.Tokens;
import com.ll.auth.model.vo.request.TokenValidRequest;
import com.ll.auth.oAuth2.JWTProvider;
import com.ll.auth.repository.AuthRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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
            existAuth.updateRefreshToke(tokens.refreshToken());
            authRepository.save(existAuth);
            return tokens;
        }
    }
}
