package io.goorm.team02.users.controller;

import io.goorm.team02.common.dto.auth.SignupRequest;
import io.goorm.team02.common.dto.auth.SignupResponse;
import io.goorm.team02.users.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor

public class SignupController {
    private final UserService userService; 
    
    // ✅ 회원가입 API
    @PostMapping("/signup")
    //@RequestBody SignupRequest request→ 클라이언트에서 보낸 JSON 데이터를 SignupRequest 객체로 변환
    public ResponseEntity<?> signup(@RequestBody SignupRequest request) {
        // 실제 회원가입 처리 : 받은 데이터로 userservice에서 하는 회원가입처리
        //userService.registerUser(request); 
        //return ResponseEntity.ok("User registered successfully");
        SignupResponse response = userService.registerUser(request);
        return ResponseEntity.ok(response);
    }

}
