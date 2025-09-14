package io.goorm.team02.core.orders.controller;

import io.goorm.team02.core.orders.domain.Order;
import io.goorm.team02.core.orders.dto.OrderRequest;
import io.goorm.team02.core.orders.service.OrderService;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createOrder(@RequestBody OrderRequest orderRequest) {
        Order savedOrder = orderService.createOrder(orderRequest);

        Map<String, Object> response = new HashMap<>();
        response.put("orderId", savedOrder.getId());

        return ResponseEntity.ok(response);
    }
}
