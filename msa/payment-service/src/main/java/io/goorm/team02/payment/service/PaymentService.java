package io.goorm.team02.payment.service;

import com.fasterxml.jackson.databind.JsonNode;
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

    private final PaymentRepository paymentRepository;
    private final PaymentEventPublisher paymentEventPublisher;
    private final RestTemplate restTemplate;

    private final String TOSS_SECRET_KEY = "test_gsk_docs_OaPz8L5KdmQXkzRz3y47BMw6";
    private final String TOSS_PAYMENTS_URL = "https://api.tosspayments.com/v1/payments/confirm";

    public PaymentService(PaymentRepository paymentRepository,
                          PaymentEventPublisher paymentEventPublisher,
                          RestTemplate restTemplate) {
        this.paymentRepository = paymentRepository;
        this.paymentEventPublisher = paymentEventPublisher;
        this.restTemplate = restTemplate;
    }

    public PaymentResponse confirmPayment(PaymentConfirmRequest request) throws PaymentException {
        try {
            // 기본 검증
            if (request == null || request.getPaymentKey() == null || request.getOrderId() == null || request.getAmount() == null) {
                throw new PaymentException("invalid request");
            }

            String orderId = request.getOrderId();
            BigDecimal amount = request.getAmount();
            String userId = null;
            try {
                // getUserId가 없을 수도 있으므로 안전하게 시도
                userId = (String) (request.getClass().getMethod("getUserId") != null ?
                        request.getClass().getMethod("getUserId").invoke(request) : null);
            } catch (NoSuchMethodException ignored) {
                userId = null;
            } catch (Exception ignored) {
                userId = null;
            }

            // Toss API 호출로 결제 검증 (paymentKey, orderId, amount)
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            String basicAuth = Base64.getEncoder().encodeToString((TOSS_SECRET_KEY + ":").getBytes());
            headers.set("Authorization", "Basic " + basicAuth);

            Map<String, Object> body = new HashMap<>();
            body.put("paymentKey", request.getPaymentKey());
            body.put("orderId", orderId);
            body.put("amount", amount.longValue());

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            ResponseEntity<JsonNode> response = restTemplate.exchange(
                    TOSS_PAYMENTS_URL,
                    HttpMethod.POST,
                    entity,
                    JsonNode.class
            );

            PaymentResponse paymentResponse = new PaymentResponse();
            paymentResponse.setOrderId(orderId);
            paymentResponse.setAmount(amount);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                JsonNode respBody = response.getBody();

                String respOrderId = respBody.has("orderId") ? respBody.get("orderId").asText() : orderId;
                long respAmount = respBody.has("amount") ? respBody.get("amount").asLong() : amount.longValue();

                if (!orderId.equals(respOrderId) || amount.longValue() != respAmount) {

                    paymentResponse.setStatus("FAIL: validation mismatch");
                    return paymentResponse;
                }

                paymentResponse.setStatus("SUCCESS");
                if (respBody.has("paymentKey")) paymentResponse.setPaymentKey(respBody.get("paymentKey").asText());
                if (respBody.has("method")) paymentResponse.setPgProvider(respBody.get("method").asText());
                if (respBody.has("tid")) paymentResponse.setPgTid(respBody.get("tid").asText());

                paymentResponse.setPaymentMethod(respBody.has("method") ? respBody.get("method").asText() : "CARD");

                paymentRepository.savePayment(
                        orderId,
                        userId,
                        amount,
                        paymentResponse.getPaymentKey(),
                        paymentResponse.getPgProvider(),
                        paymentResponse.getPaymentMethod()
                );

                paymentEventPublisher.publishPaymentCompletedEvent(
                        orderId,
                        userId,
                        amount
                );

                return paymentResponse;
            } else {
                paymentResponse.setStatus("FAIL: Toss confirm failed");
                return paymentResponse;
            }

        } catch (PaymentException pe) {
            throw pe;
        } catch (Exception e) {
            throw new PaymentException("Payment processing failed: " + e.getMessage());
        }
    }

    public void processPaymentFromEvent(String orderId, BigDecimal totalAmount, JsonNode eventNode) {
        // 중복 결제 확인
        if (paymentRepository.existsByOrderId(orderId)) return;

        String userId = null;
        try {
            userId = eventNode.has("userId") ? eventNode.get("userId").asText() : null;
        } catch (Exception ignored) {
            userId = null;
        }

        String paymentKey = "EVENT_" + orderId;
        String pgProvider = "TEST_PG";
        String paymentMethod = "CARD";

        paymentRepository.savePayment(
                orderId,
                userId,
                totalAmount,
                paymentKey,
                pgProvider,
                paymentMethod
        );

        paymentEventPublisher.publishPaymentCompletedEvent(
                orderId,
                userId,
                totalAmount
        );
    }

    public static class PaymentException extends Exception {
        public PaymentException(String message) { super(message); }
    }
}
