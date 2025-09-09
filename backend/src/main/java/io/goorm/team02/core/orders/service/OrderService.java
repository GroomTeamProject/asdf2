package io.goorm.team02.core.orders.service;

import io.goorm.team02.core.orders.domain.Order;
import io.goorm.team02.core.orders.domain.OrderItem;
import io.goorm.team02.core.orders.domain.enums.OrderStatus;
import io.goorm.team02.core.orders.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OrderService {

	private final OrderRepository orderRepository;

	public OrderService(OrderRepository orderRepository) {
		this.orderRepository = orderRepository;
	}

	@Transactional
	public Order createOrder(Order order) {
		order.setStatus(OrderStatus.COOKING);
		order.getItems().forEach(item -> item.setOrder(order));
		return orderRepository.save(order);
	}

	public List<Order> getAllOrders() {
		return orderRepository.findAll();
	}

	@Transactional
	public void cancelOrder(Long orderId) {
		Order order = orderRepository.findById(orderId)
				.orElseThrow(() -> new RuntimeException("주문이 존재하지 않습니다."));
		if (order.getStatus() == OrderStatus.COOKING) {
			order.setStatus(OrderStatus.CANCELLED);
			orderRepository.save(order);
		} else {
			throw new RuntimeException("이미 완료된 주문은 취소할 수 없습니다.");
		}
	}

	@Transactional
	public void updateStatus(Long orderId, OrderStatus status) {
		Order order = orderRepository.findById(orderId)
				.orElseThrow(() -> new RuntimeException("주문이 존재하지 않습니다."));
		order.setStatus(status);
		orderRepository.save(order);
	}
}
