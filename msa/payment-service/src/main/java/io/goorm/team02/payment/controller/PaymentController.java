package io.goorm.team02.payment.controller;

import io.goorm.team02.payment.dto.PaymentConfirmRequest;
import io.goorm.team02.payment.dto.PaymentResponse;
import io.goorm.team02.payment.event.PaymentCompletedEvent;
import io.goorm.team02.payment.service.PaymentService;
import io.goorm.team02.payment.service.SseEmitterService;
import io.goorm.team02.common.dto.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final Logger log = LoggerFactory.getLogger(PaymentController.class);

    private final PaymentService paymentService;
    private final SseEmitterService sseEmitterService;

    public PaymentController(PaymentService paymentService, SseEmitterService sseEmitterService) {
        this.paymentService = paymentService;
        this.sseEmitterService = sseEmitterService;
    }

    // =========================
    // SSE 연결
    // =========================
    @GetMapping("/sse/connect")
    public SseEmitter connectSSE(@RequestParam(value = "token", required = false) String token) {
        log.info("[SSE] 연결 요청 token={}", token);
        return sseEmitterService.createEmitter(30 * 60 * 1000L);
    }

    // =========================
    // 결제 콜백
    // =========================
    @PostMapping("/callback")
    public ApiResponse<PaymentResponse> confirmPayment(@RequestBody PaymentConfirmRequest request) {
        log.info("==== 결제 콜백 요청 데이터 확인 ====");
        log.info("paymentKey: {}, orderId: {}, amount: {}",
                request.getPaymentKey(), request.getOrderId(), request.getAmount());
        log.info("=================================");

        try {
            // 결제 승인 처리 (PaymentService 내부에서 이벤트 발행 포함)
            PaymentResponse response = paymentService.confirmPayment(request);

            log.info("==== 결제 API 응답 확인 ====");
            log.info(response.toString());
            log.info("=========================");

            PaymentCompletedEvent event = PaymentCompletedEvent.builder()
                    .paymentKey(response.getPaymentKey())
                    .orderId(response.getOrderId())
                    .amount(response.getAmount())
                    .status(response.getStatus())
                    .build();

            sseEmitterService.sendPaymentEvent(event);
            log.info("[SSE] 결제 이벤트 전송 완료: {}", event);

            return ApiResponse.ok(response);

        } catch (Exception e) {
            log.error("==== 결제 승인 실패 ====", e);

            // 실패 SSE 이벤트 전송 가능
            PaymentCompletedEvent failEvent = PaymentCompletedEvent.builder()
                    .paymentKey(request.getPaymentKey())
                    .orderId(request.getOrderId())
                    .status("FAILED")
                    .build();
            sseEmitterService.sendPaymentEvent(failEvent);
            log.info("[SSE] 결제 실패 이벤트 전송 완료: {}", failEvent);

            return ApiResponse.fail("결제 승인 실패: " + e.getMessage());
        }
    }
}
