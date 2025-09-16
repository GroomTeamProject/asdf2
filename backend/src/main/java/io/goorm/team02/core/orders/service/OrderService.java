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

			order.addItem(item);
		}

		Order savedOrder = orderRepository.saveAndFlush(order);
		System.out.println("DB에 저장된 주문 ID: " + savedOrder.getId());
		return savedOrder;
	}
}
