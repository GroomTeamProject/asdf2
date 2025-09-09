package io.goorm.team02.core.orders.controller;

import io.goorm.team02.core.orders.service.OrderService;
import io.goorm.team02.core.orders.dto.OrderRequest;
import io.goorm.team02.core.orders.domain.Order;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

	private final OrderService orderService;

	public OrderController(OrderService orderService) {
		this.orderService = orderService;
	}

	@PostMapping
	public Order createOrder(@RequestBody OrderRequest request) {
		return orderService.createOrder(request.toOrder());
	}

	@GetMapping
	public java.util.List<Order> getAllOrders() {
		return orderService.getAllOrders();
	}
}
