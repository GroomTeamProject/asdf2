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
    public ResponseEntity<String> completePayment(@RequestBody PaymentRequest request) {
        // 주문 조회
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + request.getOrderId()));

        System.out.println("paymentKey: " + request.getPaymentKey());

        // 결제 저장
        Payment payment = new Payment();
        payment.setPaymentKey(request.getPaymentKey()); 
        payment.setPgProvider(request.getPgProvider());
        payment.setPgTid(request.getPgTid());
        payment.setAmount(request.getAmount());
        payment.setOrder(order);
        payment.setStatus(PaymentStatus.COMPLETED); 

        paymentRepository.save(payment);

        return ResponseEntity.ok("Payment completed and order saved!");
    }
}
