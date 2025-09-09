package io.goorm.team02.core.orders.controller;

import io.goorm.team02.core.orders.controller.dto.OrderRequest;
import io.goorm.team02.core.orders.controller.dto.OrderResponse;
import io.goorm.team02.core.orders.service.OrderService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController implements OrderControllerDocs {

	private final OrderService orderService;

	@PostMapping
	public OrderResponse create(@RequestBody OrderRequest order) {
		return orderService.create(order);
	}

	@GetMapping
	public List<OrderResponse> getAll(@RequestParam("storeId") Long storeId) {
		return orderService.getAll(storeId);
	}

}