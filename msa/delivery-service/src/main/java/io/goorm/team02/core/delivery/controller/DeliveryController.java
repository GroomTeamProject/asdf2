package io.goorm.team02.core.delivery.controller;


import io.goorm.team02.core.delivery.controller.dto.ApiResponse;
import io.goorm.team02.core.delivery.service.DeliveryService;
import io.goorm.team02.dto.deliveries.DeliveryResponse;
import io.goorm.team02.security.annotation.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/deliveries")
@RequiredArgsConstructor
public class DeliveryController {

    private final DeliveryService deliveryService;

    @PostMapping("/{orderId}/accept")
    public ResponseEntity<ApiResponse<DeliveryResponse>> accept(@PathVariable Long orderId,  @CurrentUser Long riderId){
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.fail("권한이 없습니다."));
    }
    @PutMapping("/{orderId}/pickup")
    public ResponseEntity<ApiResponse<DeliveryResponse>> pickup(@PathVariable Long orderId, @CurrentUser Long riderId){
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.fail("권한이 없습니다."));
    }

    @PutMapping("/{orderId}/complete")
    public ResponseEntity<ApiResponse<DeliveryResponse>> complete(@PathVariable Long orderId, @CurrentUser Long riderId){
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.fail("권한이 없습니다."));
    }

    @GetMapping("/today-count")
    public ResponseEntity<Long> todayCount(@CurrentUser Long riderId) {
        return ResponseEntity.ok(deliveryService.getTodayCount(riderId));
    }

    @GetMapping("/today-income")
    public ResponseEntity<Long>  todayIncome(@CurrentUser Long riderId) {
        return ResponseEntity.ok(deliveryService.getTodayIncome(riderId));
    }

    @GetMapping("/today-avg")
    public ResponseEntity<Long>  todayAvg(@CurrentUser Long riderId) {
        return ResponseEntity.ok(deliveryService.getTodayAvg(riderId));
    }
    @GetMapping("/rider-status")
    public ResponseEntity<String>  riderStatus(@CurrentUser Long riderId) {
        return ResponseEntity.ok(deliveryService.getRiderStatus(riderId));
    }
    @GetMapping("/history")
    public ResponseEntity<ApiResponse<List<DeliveryResponse>>>  history(@CurrentUser Long riderId) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.fail("권한이 없습니다."));
    }


    @GetMapping("/currentDelivery")
    public ResponseEntity<ApiResponse<DeliveryResponse>> currentDelivery(@CurrentUser Long riderId){
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.fail("권한이 없습니다."));
    }
}
