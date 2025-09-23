// src/main/java/io/goorm/team02/core/deliveries/controller/RiderDeliveryController.java
package io.goorm.team02.core.deliveries.controller;

import io.goorm.team02.core.deliveries.controller.dto.*;
import io.goorm.team02.core.deliveries.domain.Delivery;
import io.goorm.team02.core.deliveries.domain.enums.DeliveryStatus;
import io.goorm.team02.core.deliveries.service.DeliveryQueryService;
import io.goorm.team02.core.deliveries.service.RiderDeliveryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.goorm.team02.core.deliveries.controller.dto.DeliveryListResponse;
import io.goorm.team02.core.deliveries.controller.dto.DeliveryResponse;
import static org.springframework.data.domain.Sort.Direction.DESC;


@RestController
@RequestMapping("/api/rider/deliveries")
@RequiredArgsConstructor
public class RiderDeliveryController {
    private final RiderDeliveryService cmd;
    private final DeliveryQueryService qry;

    @GetMapping
    public ResponseEntity<ApiResponse<DeliveryListResponse<DeliveryResponse>>> list(
            @PageableDefault(size=20, sort="id", direction=DESC) Pageable pageable) {
        var page = qry.listRequested(pageable);
        var body = DeliveryListResponse.of(page.map(DeliveryResponse::of));
        return ResponseEntity.ok(ApiResponse.ok(body));
    }

    @PostMapping("/{id}/accept")
    public ResponseEntity<ApiResponse<DeliveryResponse>> accept(@PathVariable Long id, @RequestBody @Valid AcceptRequest req){
        var d = cmd.accept(id, req.riderId());
        return ResponseEntity.status(201).body(ApiResponse.ok(DeliveryResponse.of(d)));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<DeliveryResponse>> reject(@PathVariable Long id, @RequestBody @Valid RejectRequest req){
        return ResponseEntity.ok(ApiResponse.ok(DeliveryResponse.of(cmd.reject(id, req.riderId(), req.reason()))));
    }

    @PutMapping("/{id}/pickup")
    public ResponseEntity<ApiResponse<DeliveryResponse>> pickup(@PathVariable Long id){
        return ResponseEntity.ok(ApiResponse.ok(DeliveryResponse.of(cmd.pickup(id))));
    }

    @PutMapping("/{id}/complete")
    public ResponseEntity<ApiResponse<DeliveryResponse>> complete(@PathVariable Long id){
        return ResponseEntity.ok(ApiResponse.ok(DeliveryResponse.of(cmd.complete(id))));
    }

    // 옵션: PATCH /status 는 두 값만 허용
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<DeliveryResponse>> patch(@PathVariable Long id, @RequestBody @Valid StatusPatchRequest req){
        return switch (req.status()) {
            case PICKED_UP -> ResponseEntity.ok(ApiResponse.ok(DeliveryResponse.of(cmd.pickup(id))));
            case DELIVERED -> ResponseEntity.ok(ApiResponse.ok(DeliveryResponse.of(cmd.complete(id))));
            default -> throw new IllegalArgumentException("allowed: PICKED_UP, DELIVERED");
        };
    }
    /**
     * 특정 라이더의 오늘(자정 기준) 완료된 배달 수익 합계를 조회한다.
     *
     * @param riderId 라이더 ID
     * @return 오늘 완료된 배달들의 배달 수수료 합계 (없으면 0)
     */
    @GetMapping("/{riderId}/today-earnings")
    public ResponseEntity<Long> getTodayEarnings(@PathVariable Long riderId) {
        Long earnings = qry.getTodayDeliveredFee(riderId);
        return ResponseEntity.ok(earnings);
    }


    @GetMapping("/{riderId}/count")
    public ResponseEntity<Long> getCountByRiderId(@PathVariable Long riderId) {
        Long count = qry.countByRiderId(riderId);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/{riderId}/avg")
    public ResponseEntity<Long> getAvgByRiderId(@PathVariable Long riderId) {
        Long avg = qry.AvgByRiderId(riderId);
        return ResponseEntity.ok(avg);
    }
}