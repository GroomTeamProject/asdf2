package io.goorm.team02.payment.controller;

import io.goorm.team02.payment.dto.PaymentConfirmRequest;
import io.goorm.team02.payment.dto.PaymentResponse;
import io.goorm.team02.payment.service.PaymentService;
import io.goorm.team02.security.annotation.CurrentUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/confirm")
    public ResponseEntity<PaymentResponse> confirmPayment(
            @RequestBody PaymentConfirmRequest request,
            @CurrentUser String userId
    ) {
        try {
            PaymentResponse response = paymentService.confirmPayment(request, userId);
            return ResponseEntity.ok(response);
        } catch (PaymentService.PaymentException e) {
            log.error("Payment confirm failed for orderId={}", request.getOrderId(), e);
            PaymentResponse failResponse = new PaymentResponse();
            failResponse.setOrderId(request.getOrderId());
            failResponse.setAmount(request.getAmount());
            failResponse.setStatus("FAIL: Payment error");
            return ResponseEntity.status(500).body(failResponse);
        }
    }
}
