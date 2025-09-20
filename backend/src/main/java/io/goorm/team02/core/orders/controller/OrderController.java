package io.goorm.team02.core.orders.controller;

import io.goorm.team02.core.orders.domain.Order;
import io.goorm.team02.core.orders.dto.OrderItemResponse;
import io.goorm.team02.core.orders.dto.OrderRequest;
import io.goorm.team02.core.orders.dto.OrderResponse;
import io.goorm.team02.core.orders.service.OrderService;
import io.goorm.team02.core.payments.domain.Payment;
import io.goorm.team02.core.payments.domain.enums.PaymentStatus;
import io.goorm.team02.core.payments.repository.PaymentRepository;
import io.goorm.team02.core.payments.service.PaymentService;
import io.goorm.team02.core.orders.repository.OrderRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    // 주문 생성
    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createOrder(@RequestBody OrderRequest orderRequest) {
        Order order = orderService.createOrder(orderRequest);

        Map<String, Object> response = new HashMap<>();
        response.put("orderId", order.getId());
        response.put("orderIdString", "order-" + order.getId());

        return ResponseEntity.ok(response);
    }

    // 전체 주문 조회
    @GetMapping("/all")
    public ResponseEntity<List<OrderResponse>> getAllOrders() {
        List<OrderResponse> response = orderService.getAllOrders()
                .stream()
                .map(order -> {
                    List<OrderItemResponse> items = order.getItems().stream()
                            .map(item -> new OrderItemResponse(
                                    item.getId(),
                                    item.getProductName(),
                                    item.getQuantity(),
                                    item.getPrice()))
                            .collect(Collectors.toList());

                    return new OrderResponse(
                            order.getId(),
                            order.getCustomerName(),
                            order.getPhoneNumber(),
                            order.getAddress(),
                            order.getRequestMessage(),
                            order.getOrderMemo(),
                            order.getTotalAmount(),
                            items);
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    // 주문 취소
    @DeleteMapping("/{orderId}")
    public ResponseEntity<String> cancelOrder(@PathVariable Long orderId) {
        try {
            orderService.cancelOrder(orderId);
            return ResponseEntity.ok("주문 취소 및 결제 환불 완료");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("주문 취소 중 오류 발생: " + e.getMessage());
        }
    }
}
