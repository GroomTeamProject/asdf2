package io.goorm.team02.core.payments.controller;

import io.goorm.team02.core.orders.domain.Order;
import io.goorm.team02.core.orders.repository.OrderRepository;
import io.goorm.team02.core.payments.domain.Payment;
import io.goorm.team02.core.payments.domain.enums.PaymentStatus;
import io.goorm.team02.core.payments.dto.PaymentRequest;
import io.goorm.team02.core.payments.repository.PaymentRepository;
import io.goorm.team02.core.payments.service.PaymentService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

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

        // orders 테이블에서 최근 주문 조회
        Order order = orderRepository.findTopByOrderByIdDesc()
                .orElseThrow(() -> new IllegalArgumentException("최근 주문을 찾을 수 없습니다."));

        // Service 호출
        Payment payment = paymentService.completePayment(
                order, // order 객체 그대로 전달
                request.getPaymentKey(),
                request.getPgProvider(),
                request.getPgTid(),
                request.getAmount());

        return ResponseEntity.ok(payment);
    }
}
