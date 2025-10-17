package io.goorm.team02.core.owner.auth.controller;

import io.goorm.team02.core.owner.auth.service.JwtTokenService;
import io.goorm.team02.core.owner.stores.domain.TempUser;
import io.goorm.team02.core.owner.stores.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/test/auth")
@RequiredArgsConstructor
@Slf4j
public class TestAuthController {

    private final JwtTokenService jwtTokenService;
    private final UserRepository userRepository;

    /**
     * 특정 사용자 ID로 테스트 토큰 생성
     */
    @PostMapping("/token")
    public ResponseEntity<Map<String, String>> generateTestToken(@RequestParam Long userId) {
        TempUser user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));

        String token = jwtTokenService.generateToken(user.getId(), user.getName());

        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        response.put("userId", user.getId().toString());
        response.put("name", user.getName());
        response.put("email", user.getEmail());

        log.info("테스트 토큰 생성 완료 - userId: {}, name: {}", user.getId(), user.getName());

        return ResponseEntity.ok(response);
    }

    /**
     * 이메일로 테스트 토큰 생성
     */
    @PostMapping("/token/email")
    public ResponseEntity<Map<String, String>> generateTestTokenByEmail(@RequestParam String email) {
        TempUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + email));

        String token = jwtTokenService.generateToken(user.getId(), user.getName());

        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        response.put("userId", user.getId().toString());
        response.put("name", user.getName());
        response.put("email", user.getEmail());

        log.info("테스트 토큰 생성 완료 - email: {}, name: {}", user.getEmail(), user.getName());

        return ResponseEntity.ok(response);
    }

    /**
     * 임의의 테스트 사용자로 토큰 생성
     */
    @PostMapping("/token/dummy")
    public ResponseEntity<Map<String, String>> generateDummyToken() {
        Long dummyUserId = 999L;
        String dummyName = "테스트사용자";

        String token = jwtTokenService.generateToken(dummyUserId, dummyName);

        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        response.put("userId", dummyUserId.toString());
        response.put("name", dummyName);
        response.put("note", "더미 사용자용 테스트 토큰입니다.");

        log.info("더미 테스트 토큰 생성 완료 - userId: {}, name: {}", dummyUserId, dummyName);

        return ResponseEntity.ok(response);
    }

    /**
     * 모든 사용자 목록 조회 (테스트용)
     */
    @GetMapping("/users")
    public ResponseEntity<List<Map<String, Object>>> getAllUsers() {
        List<TempUser> users = userRepository.findAll();
        List<Map<String, Object>> userList = users.stream()
                .map(user -> {
                    Map<String, Object> userInfo = new HashMap<>();
                    userInfo.put("id", user.getId());
                    userInfo.put("name", user.getName());
                    userInfo.put("email", user.getEmail());
                    userInfo.put("userType", user.getUserType());
                    userInfo.put("isActive", user.getIsActive());
                    return userInfo;
                })
                .toList();

        return ResponseEntity.ok(userList);
    }
}