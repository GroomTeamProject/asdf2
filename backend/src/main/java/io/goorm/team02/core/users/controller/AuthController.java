package io.goorm.team02.core.users.controller;

import io.goorm.team02.core.users.controller.dto.LoginRequest;
import io.goorm.team02.core.users.controller.dto.LoginResponse;
import io.goorm.team02.core.users.controller.dto.SignupRequest;
import io.goorm.team02.core.users.service.AuthService;
import io.goorm.team02.core.users.service.UserService;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
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
        try {
            String token = authService.login(request.getEmail(), request.getPassword());
            return ResponseEntity.ok(Map.of("token", token));
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                .body(Map.of("error", ex.getMessage()));
        }
    }
}
