package io.goorm.team02.core.orders.service;

import io.goorm.team02.core.orders.domain.Order;
import io.goorm.team02.core.orders.domain.OrderItem;
import io.goorm.team02.core.orders.dto.OrderItemResponse;
import io.goorm.team02.core.orders.dto.OrderRequest;
import io.goorm.team02.core.orders.dto.OrderResponse;
import io.goorm.team02.core.orders.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class OrderService {

	private final OrderRepository orderRepository;

	public OrderService(OrderRepository orderRepository) {
		this.orderRepository = orderRepository;
	}

	public List<Order> getOrders() {
		return orderRepository.findAll();
	}

	public List<OrderResponse> getOrderResponses() {
		List<Order> orders = orderRepository.findAll();

		return orders.stream()
				.map(order -> new OrderResponse(
						order.getId(),
						order.getCustomerName(),
						order.getPhoneNumber(),
						order.getAddress(),
						order.getRequestMessage(),
						order.getOrderMemo(),
						order.getTotalAmount(),
						order.getItems().stream()
								.map(item -> new OrderItemResponse(
										item.getId(), // Long
										item.getProductName(), // String, menuName
										item.getQuantity(), // int
										item.getPrice() // BigDecimal
								))
								.toList()))
				.toList();
	}

	@Transactional
	public Order createOrder(OrderRequest request) {
		// Order 엔티티 생성
		Order order = new Order();
		order.setCustomerName(request.getCustomerName());
		order.setPhoneNumber(request.getPhoneNumber());
		order.setAddress(request.getAddress());
		order.setRequestMessage(request.getRequestMessage());
		order.setOrderMemo(request.getOrderMemo());
		order.setTotalAmount(request.getTotalAmount());

		// OrderItem 생성
		List<OrderItem> items = request.getItems().stream().map(itemReq -> {
			OrderItem item = new OrderItem();
			item.setMenuId(itemReq.getMenuId());
			item.setName(itemReq.getProductName());
			item.setQuantity(itemReq.getQuantity());
			item.setPrice(itemReq.getPrice());
			item.setOrder(order); // 양방향 매핑
			return item;
		}).toList();

		order.setItems(items);

		// DB 저장
		return orderRepository.save(order);
	}

	@Transactional
	public void cancelOrder(Long orderId) {
		Order order = orderRepository.findById(orderId)
				.orElseThrow(() -> new IllegalArgumentException("해당 주문이 존재하지 않습니다: " + orderId));

		if (!order.canCancel()) {
			throw new IllegalStateException("주문을 취소할 수 없습니다.");
		}

		order.setStatus("CANCELLED");
	}

}
