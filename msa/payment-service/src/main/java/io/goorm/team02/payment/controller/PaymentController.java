package io.goorm.team02.core.payments.controller;

import io.goorm.team02.core.payments.dto.PaymentConfirmRequest;
import io.goorm.team02.core.payments.dto.PaymentResponse;
import io.goorm.team02.core.payments.service.PaymentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /**
     * Toss 결제 콜백 처리
     */
    @PostMapping("/callback")
    public ResponseEntity<?> confirmPayment(@RequestBody PaymentConfirmRequest request) {
        System.out.println("==== 결제 콜백 요청 데이터 확인 ====");
        System.out.println("paymentKey: " + request.getPaymentKey());
        System.out.println("orderId: " + request.getOrderId());
        System.out.println("amount: " + request.getAmount());
        System.out.println("=================================");

        try {
            // --- 결제 승인 처리 ---
            PaymentResponse response = paymentService.confirmPayment(request);

            System.out.println("==== 결제 완료 응답 확인 ====");
            System.out.println("결제 상태: " + response.getStatus());
            System.out.println("결제 키: " + response.getPaymentKey());
            System.out.println("주문 ID: " + response.getOrderId());
            System.out.println("금액: " + response.getAmount());
            System.out.println("===============================");

            // --- 프론트에 바로 상태 전달 ---
            return ResponseEntity.ok(Map.of(
                    "status", response.getStatus(),       // COMPLETED 또는 FAILED
                    "paymentKey", response.getPaymentKey(),
                    "orderId", response.getOrderId(),
                    "amount", response.getAmount()
            ));

        } catch (Exception e) {
            System.out.println("==== 결제 승인 실패 ====");
            e.printStackTrace();
            System.out.println("=======================");

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "status", "FAILED",
                            "error", "결제 승인 실패",
                            "detail", e.getMessage()
                    ));
        }
    }
}
