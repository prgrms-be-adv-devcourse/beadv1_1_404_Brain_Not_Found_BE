package com.ll.auth.service;

import com.ll.auth.model.entity.Auth;
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

//    public Tokens refreshToken(TokenValidRequest request){
//
//        log.info(request.userCode());
//        log.info(request.refreshToken());
//        log.info("refershToken Function");
//        String existRefreshToken = authRepository.findByUserCode(request.userCode()).orElseThrow(TokenNotFoundException::new).getRefreshToken();
//
//        log.info("existRefreshToken Function");
//        log.info(existRefreshToken);
//        if(existRefreshToken.equals(request.refreshToken())){
//            throw new TokenNotFoundException();
//        }
//        else{
//            Tokens tokens = jWTProvider.createToken(request.userCode());
//            Auth auth = Auth.builder().refreshToken(tokens.refreshToken())
//                    .userCode(request.userCode()).build();
//            save(auth);
//            return tokens;
//        }
//    }
}
