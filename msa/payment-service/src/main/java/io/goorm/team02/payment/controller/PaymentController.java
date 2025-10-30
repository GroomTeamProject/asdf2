package io.goorm.team02.payment.controller;

import io.goorm.team02.payment.dto.PaymentConfirmRequest;
import io.goorm.team02.payment.dto.PaymentResponse;
import io.goorm.team02.payment.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    // 토스 결제 요청 처리
    @PostMapping("/confirm")
    public ResponseEntity<PaymentResponse> confirmPayment(@RequestBody PaymentConfirmRequest request) {
        try {
            PaymentResponse response = paymentService.confirmPayment(request);
            return ResponseEntity.ok(response);
        } catch (PaymentService.PaymentException e) {
            PaymentResponse failResponse = new PaymentResponse(
                    request.getOrderId(),
                    request.getAmount(),
                    "FAIL: " + e.getMessage(),
                    null,
                    null,
                    null,
                    null
            );
            return ResponseEntity.status(500).body(failResponse);
        }
    }
}
