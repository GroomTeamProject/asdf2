package io.goorm.team02.core.orders.service;

import io.goorm.team02.core.orders.domain.Order;
import io.goorm.team02.core.orders.domain.OrderItem;
import io.goorm.team02.core.orders.dto.OrderItemResponse;
import io.goorm.team02.core.orders.dto.OrderRequest;
import io.goorm.team02.core.orders.dto.OrderResponse;
import io.goorm.team02.core.orders.repository.OrderRepository;
import io.goorm.team02.core.payments.domain.Payment;
import io.goorm.team02.core.payments.domain.enums.PaymentStatus;
import io.goorm.team02.core.payments.repository.PaymentRepository;
import io.goorm.team02.core.payments.service.PaymentService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class OrderService {

	private final OrderRepository orderRepository;
	private final PaymentRepository paymentRepository;
	private final PaymentService paymentService;

	public OrderService(OrderRepository orderRepository,
			PaymentRepository paymentRepository,
			PaymentService paymentService) {
		this.orderRepository = orderRepository;
		this.paymentRepository = paymentRepository;
		this.paymentService = paymentService;
	}

	public List<Order> getAllOrders() {
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
										item.getId(),
										item.getProductName(),
										item.getQuantity(),
										item.getPrice()))
								.toList()))
				.toList();
	}

	@Transactional
	public Order createOrder(OrderRequest request) {
		Order order = new Order();
		order.setCustomerName(request.getCustomerName());
		order.setPhoneNumber(request.getPhoneNumber());
		order.setAddress(request.getAddress());
		order.setRequestMessage(request.getRequestMessage());
		order.setOrderMemo(request.getOrderMemo());
		order.setTotalAmount(request.getTotalAmount());

		List<OrderItem> items = request.getItems().stream().map(itemReq -> {
			OrderItem item = new OrderItem();
			item.setMenuId(itemReq.getMenuId());
			item.setName(itemReq.getProductName());
			item.setQuantity(itemReq.getQuantity());
			item.setPrice(itemReq.getPrice());
			item.setOrder(order);
			return item;
		}).toList();

		order.setItems(items);
		return orderRepository.save(order);
	}

	@Transactional
	public void cancelOrder(Long orderId) {
		Order order = orderRepository.findById(orderId)
				.orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다: " + orderId));

		if (!order.canCancel()) {
			throw new IllegalStateException("이미 취소된 주문이거나 취소할 수 없는 상태입니다.");
		}

		Payment payment = order.getPayment();
		if (payment != null && payment.getStatus() != PaymentStatus.CANCELLED) {
			paymentService.cancelPayment(payment.getPaymentKey(), payment.getAmount());
			payment.setStatus(PaymentStatus.CANCELLED);
			paymentRepository.save(payment);
		}

		order.setStatus("CANCELLED");
		orderRepository.save(order);
	}

}
