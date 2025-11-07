package io.goorm.team02.users.service;

import io.goorm.team02.users.domain.User;
import io.goorm.team02.users.repository.UserinfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthVerificationService {

    private final AuthenticationManager authenticationManager;
    private final UserinfoRepository userRepository;

    public Map<String, Object> verifyLogin(String email, String password) {
        try {
            // 1️⃣ 사용자 정보 조회
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (!user.getIsActive()) {
                throw new RuntimeException("탈퇴한 계정입니다.");
            }

            // 2️⃣ Spring Security를 통한 비밀번호 인증
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password)
            );

            // 3️⃣ 성공 시 사용자 정보 반환 (JWT 발급은 Gateway가 함)
            Map<String, Object> result = new HashMap<>();
            result.put("id", user.getId());
            result.put("email", user.getEmail());
            result.put("name", user.getName());
            result.put("userType", user.getUserType().name());
            return result;

        } catch (AuthenticationException e) {
            throw new RuntimeException("Invalid email or password");
        }
    }
}
