package io.goorm.team02.payment.service;

import com.fasterxml.jackson.databind.JsonNode;
import io.goorm.team02.dto.orders.OrderResponse;
import io.goorm.team02.payment.client.PaymentServiceClient;
import io.goorm.team02.payment.dto.PaymentConfirmRequest;
import io.goorm.team02.payment.dto.PaymentResponse;
import io.goorm.team02.payment.event.PaymentEventPublisher;
import io.goorm.team02.payment.repository.PaymentRepository;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
public class PaymentService {

    private final PaymentServiceClient paymentServiceClient;
    private final PaymentRepository paymentRepository;
    private final PaymentEventPublisher paymentEventPublisher;
    private final RestTemplate restTemplate;

    // 토스 테스트 키
    private final String TOSS_CLIENT_KEY = "test_gck_docs_Ovk5rk1EwkEbP0W43n07xlzm";
    private final String TOSS_SECRET_KEY = "test_gsk_docs_OaPz8L5KdmQXkzRz3y47BMw6";
    private final String TOSS_PAYMENTS_URL = "https://api.tosspayments.com/v1/payments/confirm";

    public PaymentService(PaymentServiceClient paymentServiceClient,
                          PaymentRepository paymentRepository,
                          PaymentEventPublisher paymentEventPublisher,
                          RestTemplate restTemplate) {
        this.paymentServiceClient = paymentServiceClient;
        this.paymentRepository = paymentRepository;
        this.paymentEventPublisher = paymentEventPublisher;
        this.restTemplate = restTemplate;
    }

    // 실제 토스 결제 처리
    public PaymentResponse confirmPayment(PaymentConfirmRequest request) throws PaymentException {
        // 1. 주문 조회
        OrderResponse order = paymentServiceClient.getOrder(request.getOrderId());
        if (order == null || !"CREATED".equalsIgnoreCase(order.status())) {
            return new PaymentResponse(request.getOrderId(),
                    request.getAmount(),
                    "FAIL: Invalid order status",
                    null, null, null, null);
        }

        // 2. 이미 결제 확인
        if (paymentRepository.existsByOrderId(request.getOrderId())) {
            return new PaymentResponse(request.getOrderId(),
                    request.getAmount(),
                    "FAIL: Already paid",
                    null, null, null, null);
        }

        try {
            // 3. 토스 API 호출
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            String basicAuth = Base64.getEncoder().encodeToString((TOSS_SECRET_KEY + ":").getBytes());
            headers.set("Authorization", "Basic " + basicAuth);

            Map<String, Object> body = new HashMap<>();
            body.put("paymentKey", request.getPaymentKey());
            body.put("orderId", request.getOrderId());
            body.put("amount", request.getAmount());

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            ResponseEntity<JsonNode> response = restTemplate.exchange(
                    TOSS_PAYMENTS_URL,
                    HttpMethod.POST,
                    entity,
                    JsonNode.class
            );

            if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
                throw new PaymentException("Payment failed via Toss API");
            }

            JsonNode responseBody = response.getBody();
            String pgProvider = responseBody.get("method").asText();
            String paymentKey = responseBody.get("paymentKey").asText();
            String pgTid = responseBody.get("tid").asText();

            // 4. DB 저장
            paymentRepository.savePayment(
                    request.getOrderId(),
                    request.getUserId(),
                    request.getAmount(),
                    paymentKey,
                    pgProvider,
                    request.getPaymentMethod()
            );

            // 5. 이벤트 발행
            paymentEventPublisher.publishPaymentCompletedEvent(
                    String.valueOf(request.getOrderId()),
                    String.valueOf(request.getUserId()),
                    request.getAmount()
            );

            return new PaymentResponse(
                    request.getOrderId(),
                    request.getAmount(),
                    "SUCCESS",
                    paymentKey,
                    pgProvider,
                    request.getPaymentMethod(),
                    pgTid
            );

        } catch (Exception e) {
            throw new PaymentException("Payment processing failed: " + e.getMessage());
        }
    }

    // 이벤트 기반 결제 처리 (OrderEventListener용)
    public void processPaymentFromEvent(Long orderId, Integer totalAmount, JsonNode eventNode) {
        String userId = eventNode.get("userId").asText();
        if (paymentRepository.existsByOrderId(orderId.toString())) return;

        String paymentKey = "EVENT_" + orderId;
        String pgProvider = "TEST_PG";
        String paymentMethod = "CARD";

        paymentRepository.savePayment(
                orderId.toString(),
                userId,
                BigDecimal.valueOf(totalAmount),
                paymentKey,
                pgProvider,
                paymentMethod
        );

        paymentEventPublisher.publishPaymentCompletedEvent(
                String.valueOf(orderId),
                String.valueOf(userId),
                BigDecimal.valueOf(totalAmount)
        );
    }

    public static class PaymentException extends Exception {
        public PaymentException(String message) { super(message); }
    }
}
