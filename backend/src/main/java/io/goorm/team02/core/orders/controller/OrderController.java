package io.goorm.team02.core.orders.controller;

import io.goorm.team02.core.orders.domain.Order;
import io.goorm.team02.core.orders.dto.OrderItemResponse;
import io.goorm.team02.core.orders.dto.OrderRequest;
import io.goorm.team02.core.orders.dto.OrderResponse;
import io.goorm.team02.core.orders.service.OrderService;
import io.goorm.team02.core.orders.repository.OrderRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;
    private final OrderRepository orderRepository;

    public OrderController(OrderService orderService, OrderRepository orderRepository) {
        this.orderService = orderService;
        this.orderRepository = orderRepository;
    }

    // 주문 생성
    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createOrder(@RequestBody OrderRequest orderRequest) {
        Order order = orderService.createOrder(orderRequest);

        Map<String, Object> response = new HashMap<>();
        response.put("orderId", order.getId());
        response.put("orderIdString", "order-" + order.getId()); // PG용 문자열 ID

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> getAllOrders() {
        List<OrderResponse> response = orderRepository.findAll().stream().map(order -> {
            // OrderItemResponse 매핑
            List<OrderItemResponse> items = order.getItems().stream()
                    .map(item -> new OrderItemResponse(
                            item.getId(),
                            item.getProductName(),
                            item.getQuantity(),
                            item.getPrice()
            ))
                    .collect(Collectors.toList());

            // OrderResponse 생성
            return new OrderResponse(
                    order.getId(),
                    order.getCustomerName(),
                    order.getPhoneNumber(),
                    order.getAddress(),
                    order.getRequestMessage(),
                    order.getOrderMemo(),
                    order.getTotalAmount(),
                    items);
        }).collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /*
     * // 특정 주문 조회
     * 
     * @GetMapping("/{orderId}")
     * public ResponseEntity<Order> getOrder(@PathVariable Long orderId) {
     * return orderService.getOrder(orderId)
     * .map(ResponseEntity::ok)
     * .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
     * }
     */

    // 주문 취소
    @DeleteMapping("/{orderId}")
    public ResponseEntity<String> cancelOrder(@PathVariable Long orderId) {
        try {
            orderService.cancelOrder(orderId);
            return ResponseEntity.ok("주문 취소 성공");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
