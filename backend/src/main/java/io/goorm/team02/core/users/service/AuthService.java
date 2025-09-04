package io.goorm.team02.core.users.service;

import io.goorm.team02.core.common.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    public String login(String email, String password) {
        try {
            // 이메일/비밀번호 인증
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password)
            );

            // 인증 성공 → JWT 발급
            return jwtTokenProvider.generateToken(authentication);

        } catch (AuthenticationException e) {
            throw new RuntimeException("Invalid email or password");
        }
    }
}