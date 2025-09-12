package io.goorm.team02.core.orders.controller;

import io.goorm.team02.core.orders.domain.Order;
import io.goorm.team02.core.orders.dto.OrderRequest;
import io.goorm.team02.core.orders.service.OrderService;

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
	public ResponseEntity<?> createOrder(@RequestBody OrderRequest orderRequest) {
		Order savedOrder = orderService.createOrder(orderRequest);
		System.out.println("생성된 주문 ID: " + savedOrder.getId()); // 🔹 로그 확인
		return ResponseEntity.ok(savedOrder);
	}

}
