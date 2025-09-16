package io.goorm.team02.core.payments.controller;

import io.goorm.team02.core.orders.domain.Order;
import io.goorm.team02.core.orders.repository.OrderRepository;
import io.goorm.team02.core.payments.domain.Payment;
import io.goorm.team02.core.payments.domain.enums.PaymentStatus;
import io.goorm.team02.core.payments.dto.PaymentRequest;
import io.goorm.team02.core.payments.repository.PaymentRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    public PaymentController(PaymentRepository paymentRepository, OrderRepository orderRepository) {
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
    }

    @PostMapping("/callback")
    @Transactional
    public ResponseEntity<Payment> completePayment(@RequestBody PaymentRequest request) {
        // 1주문 조회
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + request.getOrderId()));

        // 금액 검증
        if (request.getAmount().compareTo(order.getTotalAmount()) != 0) {
            throw new IllegalArgumentException("결제 금액 불일치");
        }

        // Payment 조회 또는 새로 생성
        Payment payment = paymentRepository.findByPaymentKey(request.getPaymentKey())
                .orElseGet(() -> {
                    Payment p = new Payment();
                    p.setPaymentKey(request.getPaymentKey());
                    p.setOrder(order);
                    return p;
                });

        // 결제 정보 업데이트
        payment.setAmount(request.getAmount());
        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setPgProvider(request.getPgProvider());
        payment.setPgTid(request.getPgTid());

        paymentRepository.save(payment);

        return ResponseEntity.ok(payment);
    }
}
