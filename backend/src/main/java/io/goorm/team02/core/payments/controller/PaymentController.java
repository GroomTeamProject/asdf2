package io.goorm.team02.core.payments.controller;

import io.goorm.team02.core.payments.domain.Payment;
import io.goorm.team02.core.payments.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// 프론트에서 /api/payments로 요청이 들어온다고 가정
@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "http://localhost:5173")
public class PaymentController {

    private final PaymentService paymentService;

    @Autowired
    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    // POST 요청 처리
    @PostMapping
    public ResponseEntity<PaymentResponse> placePayment(@RequestBody PaymentRequest paymentRequest) {
        try {
            Payment payment = paymentService.createPayment(paymentRequest);

            PaymentResponse response = new PaymentResponse(
                    payment.getId(),
                    payment.getStatus().name(),
                    "Payment processed successfully");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            PaymentResponse response = new PaymentResponse(
                    null,
                    "FAILED",
                    "Payment processing failed: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    // --- DTO 클래스 ---
    public static class PaymentRequest {
        private Long orderId;
        private String method; // CARD, CASH, KAKAO_PAY 등
        private double amount;

        // getters & setters
        public Long getOrderId() {
            return orderId;
        }

        public void setOrderId(Long orderId) {
            this.orderId = orderId;
        }

        public String getMethod() {
            return method;
        }

        public void setMethod(String method) {
            this.method = method;
        }

        public double getAmount() {
            return amount;
        }

        public void setAmount(double amount) {
            this.amount = amount;
        }
    }

    public static class PaymentResponse {
        private Long paymentId;
        private String status;
        private String message;

        public PaymentResponse(Long paymentId, String status, String message) {
            this.paymentId = paymentId;
            this.status = status;
            this.message = message;
        }

        // getters & setters
        public Long getPaymentId() {
            return paymentId;
        }

        public void setPaymentId(Long paymentId) {
            this.paymentId = paymentId;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}
