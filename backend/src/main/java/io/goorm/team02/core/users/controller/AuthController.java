package io.goorm.team02.core.users.controller;

import io.goorm.team02.core.users.controller.dto.LoginRequest;
import io.goorm.team02.core.users.controller.dto.SignupRequest;
import io.goorm.team02.core.users.service.AuthService;
import io.goorm.team02.core.users.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    // ✅ 회원가입 API
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignupRequest request) {
        userService.registerUser(request);
        return ResponseEntity.ok("User registered successfully");
    }

    // ✅ 로그인 API
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        String token = authService.login(request);
        return ResponseEntity.ok(Map.of("token", token));
    }
}
