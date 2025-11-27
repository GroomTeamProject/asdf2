package io.goorm.team02.core.delivery.controller;

import io.goorm.team02.core.delivery.security.jwt.JwtUtils;
import io.goorm.team02.core.delivery.controller.dto.ApiResponse;
import io.goorm.team02.core.delivery.entity.Delivery;
import io.goorm.team02.core.delivery.mapper.DeliveryMapper;
import io.goorm.team02.core.delivery.service.DeliveryService;
import io.goorm.team02.dto.deliveries.DeliveryResponse;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

// delivery controller
@Slf4j
@RestController
@RequestMapping("/api/deliveries")
@RequiredArgsConstructor
public class DeliveryController {
    @Value("${jwt.secret}")
    private String jwtSecret;
    private final DeliveryService deliveryService;
    private final DeliveryMapper deliveryMapper;
    private final JwtUtils jwtUtils;

    @PostMapping("/{orderId}/accept")
    public ResponseEntity<ApiResponse<DeliveryResponse>> accept(
            @PathVariable Long orderId,
            HttpServletRequest request
    ) {
        // JWT 추출
        String token = jwtUtils.extractToken(request);

        // Claims 파싱 (검증까지 자동 포함)
        Claims claims = jwtUtils.parse(token);

        // userId 추출
        Long userId = jwtUtils.getUserId(claims);
        log.info("[accept] request for orderId={}, riderId={}", orderId, userId);
        log.info("jwt_secret = {}", jwtSecret);
        Delivery delivery = deliveryService.accept(userId, orderId);
        DeliveryResponse response = deliveryMapper.toResponse(delivery);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(response));
    }

    @PutMapping("/{orderId}/pickup")
    public ResponseEntity<ApiResponse<DeliveryResponse>> pickup(
            @PathVariable Long orderId,
            HttpServletRequest request
    ) {
        // JWT 추출
        String token = jwtUtils.extractToken(request);

        // Claims 파싱 (검증까지 자동 포함)
        Claims claims = jwtUtils.parse(token);

        // userId 추출
        Long userId = jwtUtils.getUserId(claims);
        log.info("[pickup] request for orderId={}, riderId={}", orderId, userId);
        log.info("jwt_secret = {}", jwtSecret);
        Delivery delivery = deliveryService.pickup(userId, orderId);
        DeliveryResponse response = deliveryMapper.toResponse(delivery);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PutMapping("/{orderId}/complete")
    public ResponseEntity<ApiResponse<DeliveryResponse>> complete(
            @PathVariable Long orderId,
            HttpServletRequest request
    ) {
        // JWT 추출
        String token = jwtUtils.extractToken(request);

        // Claims 파싱 (검증까지 자동 포함)
        Claims claims = jwtUtils.parse(token);

        // userId 추출
        Long userId = jwtUtils.getUserId(claims);
        log.info("[complete] request for orderId={}, riderId={}", orderId, userId);
        log.info("jwt_secret = {}", jwtSecret);
        Delivery delivery = deliveryService.complete(userId, orderId);
        DeliveryResponse response = deliveryMapper.toResponse(delivery);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/history")
    public ResponseEntity<ApiResponse<?>> history(HttpServletRequest request) {
        // JWT 추출
        String token = jwtUtils.extractToken(request);

        // Claims 파싱 (검증까지 자동 포함)
        Claims claims = jwtUtils.parse(token);

        // userId 추출
        Long userId = jwtUtils.getUserId(claims);
        log.info("[history] request for riderId={}", userId);
        log.info("jwt_secret = {}", jwtSecret);
        List<DeliveryResponse> deliveries = deliveryService.getDeliveries(userId);
        if (deliveries.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.fail("[history]: 조회 결과가 없습니다."));
        }
        return ResponseEntity.ok(ApiResponse.ok(deliveries));
    }

    @GetMapping("/current")
    public ResponseEntity<ApiResponse<?>> currentDelivery(HttpServletRequest request) {
        // JWT 추출
        String token = jwtUtils.extractToken(request);

        // Claims 파싱 (검증까지 자동 포함)
        Claims claims = jwtUtils.parse(token);

        // userId 추출
        Long userId = jwtUtils.getUserId(claims);
        log.info("[current] request for riderId={}", userId);
        log.info("jwt_secret = {}", jwtSecret);
        Optional<Delivery> deliveryOpt = deliveryService.getCurrentDelivery(userId);
        if (deliveryOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.fail("[currentDelivery]: 현재 진행 중인 배달이 없습니다."));
        }

        DeliveryResponse response = deliveryMapper.toResponse(deliveryOpt.get());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/today-count")
    public ResponseEntity<ApiResponse<BigDecimal>> todayCount(HttpServletRequest request) {
        // JWT 추출
        String token = jwtUtils.extractToken(request);

        // Claims 파싱 (검증까지 자동 포함)
        Claims claims = jwtUtils.parse(token);

        // userId 추출
        Long userId = jwtUtils.getUserId(claims);
        log.info("[today-count] request for riderId={}", userId);
        log.info("jwt_secret = {}", jwtSecret);
        return ResponseEntity.ok(ApiResponse.ok(deliveryService.getTodayCount(userId)));
    }

    @GetMapping("/today-income")
    public ResponseEntity<ApiResponse<BigDecimal>> todayIncome(HttpServletRequest request) {
        // JWT 추출
        String token = jwtUtils.extractToken(request);

        // Claims 파싱 (검증까지 자동 포함)
        Claims claims = jwtUtils.parse(token);

        // userId 추출
        Long userId = jwtUtils.getUserId(claims);
        log.info("[today-income] request for riderId={}", userId);
        log.info("jwt_secret = {}", jwtSecret);
        return ResponseEntity.ok(ApiResponse.ok(deliveryService.getTodayIncome(userId)));
    }

    @GetMapping("/today-avg")
    public ResponseEntity<ApiResponse<Long>> todayAvg(HttpServletRequest request) {
        // JWT 추출
        String token = jwtUtils.extractToken(request);

        // Claims 파싱 (검증까지 자동 포함)
        Claims claims = jwtUtils.parse(token);

        // userId 추출
        Long userId = jwtUtils.getUserId(claims);
        log.info("[today-avg] request for riderId={}", userId);
        log.info("jwt_secret = {}", jwtSecret);
        return ResponseEntity.ok(ApiResponse.ok(deliveryService.getTodayAvg(userId)));
    }

    @GetMapping("/rider-status")
    public ResponseEntity<ApiResponse<String>> riderStatus(HttpServletRequest request) {
        // JWT 추출
        String token = jwtUtils.extractToken(request);

        // Claims 파싱 (검증까지 자동 포함)
        Claims claims = jwtUtils.parse(token);

        // userId 추출
        Long userId = jwtUtils.getUserId(claims);
        log.info("[rider-status] request for riderId={}", userId);
        log.info("jwt_secret = {}", jwtSecret);
        return ResponseEntity.ok(ApiResponse.ok(deliveryService.getRiderStatus(userId)));
    }
}
