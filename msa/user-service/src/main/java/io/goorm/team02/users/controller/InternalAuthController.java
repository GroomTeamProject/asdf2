package io.goorm.team02.users.controller;

import io.goorm.team02.dto.user.LoginResponse;
import io.goorm.team02.users.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/internal")
@RequiredArgsConstructor
public class InternalAuthController {

    private final AuthService authService;

    @PostMapping("/verify")
    public LoginResponse verify(@RequestBody Map<String, String> loginRequest) {
        String email = loginRequest.get("email");
        String password = loginRequest.get("password");
        return authService.login(email, password);
    }
}
