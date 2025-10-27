package io.goorm.team02.gateway.controller;

import io.goorm.team02.gateway.service.AuthProxyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class testController {

    private final AuthProxyService authProxyService;

    @PostMapping("/login")
    public Mono<ResponseEntity<Map<String, Object>>> login(@RequestBody Map<String, Object> loginRequest) {
        return authProxyService.requestLogin(loginRequest)
                .map(ResponseEntity::ok);
    }
}
