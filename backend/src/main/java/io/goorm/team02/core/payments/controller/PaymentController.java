// src/main/java/io/goorm/team02/core/payments/controller/PaymentController.java
package io.goorm.team02.core.payments.controller;

import io.goorm.team02.core.payments.dto.PaymentRequest;
import io.goorm.team02.core.payments.dto.PaymentResponse;
import io.goorm.team02.core.payments.service.PaymentService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    public PaymentResponse createPayment(@RequestBody PaymentRequest request) {
        return paymentService.createPayment(request);
    }

    @GetMapping("/{paymentId}")
    public PaymentResponse getPayment(@PathVariable String paymentId) {
        // DB 조회 로직 예제
        return new PaymentResponse(paymentId, 50000, "READY");
    }

    @PostMapping("/{paymentId}/cancel")
    public PaymentResponse cancelPayment(@PathVariable String paymentId) {
        // DB 결제 취소 로직 예제
        return new PaymentResponse(paymentId, 50000, "CANCELLED");
    }
}
