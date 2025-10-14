package io.goorm.team02.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.goorm.team02.demo.dto.OrderResponse;
import io.goorm.team02.demo.service.DemoService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/demo")
public class DemoController {

    private final DemoService demoService;

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(demoService.getOrderById(id));
    }

}
