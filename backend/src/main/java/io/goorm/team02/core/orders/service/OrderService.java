package io.goorm.team02.core.orders.service;

import io.goorm.team02.core.orders.controller.dto.OrderRequest;
import io.goorm.team02.core.orders.controller.dto.OrderResponse;
import io.goorm.team02.core.orders.domain.Order;
import io.goorm.team02.core.orders.repository.OrderRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderService {

	private final OrderRepository orderRepository;

	public OrderResponse create(OrderRequest orderRequest) {
		Order order = orderRepository.save(orderRequest.toEntity());
		return OrderResponse.from(order);
	}

	public List<OrderResponse> getAll(Long storeId) {
		List<Order> orders = orderRepository.findAllByStoreIdWithDetails(storeId);
		
		// JPA 지연 로딩으로 orderItems와 options를 가져옴
		orders.forEach(order -> {
			order.getOrderItems().forEach(orderItem -> {
				orderItem.getOptions().size(); // 지연 로딩 트리거
			});
		});
		
		return orders.stream()
				.map(OrderResponse::from)
				.toList();
	}

}