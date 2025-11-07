package io.goorm.team02.users.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class HeaderTestController {

    @GetMapping("/headers")
    public ResponseEntity<Map<String, Object>> getHeaders(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-User-Email", required = false) String userEmail) {

        return ResponseEntity.ok(Map.of(
                "receivedUserId", userId,
                "receivedUserEmail", userEmail
        ));
    }
}
