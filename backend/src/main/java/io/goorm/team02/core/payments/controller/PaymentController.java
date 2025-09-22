package io.goorm.team02.core.payments.controller;

import io.goorm.team02.core.orders.domain.Order;
import io.goorm.team02.core.orders.domain.enums.OrderStatus;
import io.goorm.team02.core.orders.repository.OrderRepository;
import io.goorm.team02.core.payments.domain.Payment;
import io.goorm.team02.core.payments.dto.PaymentRequest;
import io.goorm.team02.core.payments.service.PaymentService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;
    private final OrderRepository orderRepository;

    public PaymentController(PaymentService paymentService, OrderRepository orderRepository) {
        this.paymentService = paymentService;
        this.orderRepository = orderRepository;
    }

    @PostMapping("/callback")
    @Transactional
    public ResponseEntity<?> completePayment(@RequestBody PaymentRequest request) {

        // Request 검증
        if (request == null || request.getPaymentKey() == null || request.getAmount() == null) {
            return ResponseEntity.badRequest().body("결제 키 또는 금액이 없습니다.");
        }

        // 최근 주문 조회
        Order order = orderRepository.findTopByOrderByIdDesc()
                .orElse(null);

        if (order == null) {
            return ResponseEntity.badRequest().body("최근 주문이 없습니다.");
        }

        // 결제 처리
        Payment payment = paymentService.completePayment(
                order,
                request.getPaymentKey(),
                request.getPgProvider(),
                request.getPgTid(),
                request.getAmount());

        return ResponseEntity.ok(payment);
    }

    /*
     * @PostMapping("/cancel/{orderId}")
     * 
     * @Transactional
     * public ResponseEntity<?> cancelPayment(@PathVariable Long orderId) {
     * Order order = orderRepository.findById(orderId)
     * .orElse(null);
     * 
     * if (order == null) {
     * return ResponseEntity.badRequest().body("주문을 찾을 수 없습니다.");
     * }
     * 
     * Payment payment = order.getPayment();
     * if (payment == null) {
     * return ResponseEntity.badRequest().body("결제 정보를 찾을 수 없습니다.");
     * }
     * 
     * try {
     * paymentService.cancelPayment(payment.getPaymentKey(), payment.getAmount());
     * } catch (Exception e) {
     * return ResponseEntity.internalServerError().body("결제 취소 실패: " +
     * e.getMessage());
     * }
     * 
     * 
     * order.setStatus(OrderStatus.CANCELLED.name());
     * orderRepository.save(order);
     * 
     * return ResponseEntity.ok("주문이 취소되었습니다.");
     * }
     */
}
