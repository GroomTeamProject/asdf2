package io.goorm.team02.payment.controller;

import io.goorm.team02.payment.client.OrderServiceClient;
import io.goorm.team02.payment.dto.PaymentConfirmRequest;
import io.goorm.team02.payment.dto.PaymentResponse;
import io.goorm.team02.payment.service.PaymentService;
import io.goorm.team02.payment.event.PaymentEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;
    private final OrderServiceClient orderServiceClient;
    private final PaymentEventPublisher eventPublisher;

    public PaymentController(PaymentService paymentService,
                             OrderServiceClient orderServiceClient,
                             PaymentEventPublisher eventPublisher) {
        this.paymentService = paymentService;
        this.orderServiceClient = orderServiceClient;
        this.eventPublisher = eventPublisher;
    }

    @PostMapping("/callback")
    public ResponseEntity<?> confirmPayment(@RequestBody PaymentConfirmRequest request) {
        System.out.println("==== 결제 콜백 요청 데이터 확인 ====");
        System.out.println("paymentKey: " + request.getPaymentKey());
        System.out.println("orderId: " + request.getOrderId());
        System.out.println("amount: " + request.getAmount());
        System.out.println("=================================");

        try {
            // 1. 주문 상태 조회
            var order = orderServiceClient.getOrderById(request.getOrderId());
            if (!"CREATED".equals(order.getStatus())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "결제를 진행할 수 없는 주문 상태", "orderStatus", order.getStatus()));
            }

            // 2. 결제 승인 처리
            PaymentResponse response = paymentService.confirmPayment(request);

            // 3. 결제 성공 이벤트 발행
            if ("COMPLETED".equals(response.getStatus())) {
                eventPublisher.publishPaymentCompleted(response);
            } else if ("FAILED".equals(response.getStatus())) {
                eventPublisher.publishPaymentFailed(response);
            }

            System.out.println("==== 결제 API 응답 확인 ====");
            System.out.println(response);
            System.out.println("=========================");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.out.println("==== 결제 승인 실패 ====");
            e.printStackTrace();
            System.out.println("=======================");

            // 결제 실패 이벤트 발행
            eventPublisher.publishPaymentFailed(new PaymentResponse(
                    null,
                    request.getOrderId(),
                    request.getUserId(),
                    request.getAmount(),
                    "KRW",
                    "FAILED",
                    request.getPaymentMethod(),
                    "TOSS"
            ));

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "error", "결제 승인 실패",
                            "detail", e.getMessage()
                    ));
        }
    }
}
