package io.goorm.team02.core.orders.service;

import io.goorm.team02.core.orders.domain.Order;
import io.goorm.team02.core.orders.domain.OrderItem;
import io.goorm.team02.core.orders.dto.OrderRequest;
import io.goorm.team02.core.orders.dto.OrderItemRequest;
import io.goorm.team02.core.orders.repository.OrderRepository;
import org.springframework.stereotype.Service;

@Service
public class OrderService {

	private final OrderRepository orderRepository;

	public OrderService(OrderRepository orderRepository) {
		this.orderRepository = orderRepository;
	}

	public Order createOrder(OrderRequest orderRequest) {
		Order order = new Order();
		order.setCustomerName(orderRequest.getCustomerName());
		order.setPhoneNumber(orderRequest.getPhoneNumber());
		order.setAddress(orderRequest.getAddress());
		order.setOrderMemo(orderRequest.getRequestMessage());
		order.setTotalAmount(orderRequest.getTotalAmount());

		// 주문 아이템 추가
		for (OrderItemRequest itemReq : orderRequest.getItems()) {
			OrderItem item = new OrderItem();
			item.setMenuId(itemReq.getMenuId());
			item.setName(itemReq.getName());
			item.setPrice(itemReq.getPrice());
			item.setProductId(itemReq.getProductId());
			item.setQuantity(itemReq.getQuantity());
			item.setProductName(itemReq.getProductName());

			// 연관관계 편의 메서드로 추가
			order.addItem(item);
		}
		//DB에 저장 (Cascade로 OrderItem도 함께 저장)
		Order savedOrder = orderRepository.saveAndFlush(order); // ✅ flush로 DB에 바로 반영

		System.out.println("DB에 저장된 주문 ID: " + savedOrder.getId());
		return savedOrder;
	}
}
