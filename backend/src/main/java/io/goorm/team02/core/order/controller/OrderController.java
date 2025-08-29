package io.goorm.team02.core.order.controller;

import io.goorm.team02.core.order.domain.Order;
import io.goorm.team02.core.order.domain.dto.OrderRequest;
import io.goorm.team02.core.order.repository.OrderRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

	private final OrderRepository orderRepository;

	@PostMapping
	public Order create(@RequestBody OrderRequest order) {
		return orderRepository.save(order.toEntity());
	}

	@GetMapping
	public List<Order> getAll() {
		return orderRepository.findAll();
	}

}
