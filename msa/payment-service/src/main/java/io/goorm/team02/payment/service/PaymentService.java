package io.goorm.team02.payment.service;

import com.fasterxml.jackson.databind.JsonNode;
import io.goorm.team02.payment.dto.PaymentConfirmRequest;
import io.goorm.team02.payment.dto.PaymentResponse;
import io.goorm.team02.payment.event.PaymentEventPublisher;
import io.goorm.team02.payment.repository.PaymentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Slf4j
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

    public PaymentResponse confirmPayment(PaymentConfirmRequest request, String userId) throws PaymentException {
        try {
            if (request == null || request.getPaymentKey() == null ||
                    request.getOrderId() == null || request.getAmount() == null) {
                throw new PaymentException("Invalid request");
            }

            String orderId = request.getOrderId();
            BigDecimal amount = request.getAmount();

            // Toss API 호출
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

                // Validation
                String respOrderId = respBody.has("orderId") ? respBody.get("orderId").asText() : orderId;
                long respAmount = respBody.has("amount") ? respBody.get("amount").asLong() : amount.longValue();

                if (!orderId.equals(respOrderId) || amount.longValue() != respAmount) {
                    paymentResponse.setStatus("FAIL: validation mismatch");
                    return paymentResponse;
                }

                // 성공 처리
                paymentResponse.setStatus("SUCCESS");
                paymentResponse.setPaymentKey(respBody.has("paymentKey") ? respBody.get("paymentKey").asText() : null);
                paymentResponse.setPaymentMethod(respBody.has("method") ? respBody.get("method").asText() : "CARD");
                paymentResponse.setPgTid(respBody.has("tid") ? respBody.get("tid").asText() : null);
                paymentResponse.setPgProvider(respBody.has("provider") ? respBody.get("provider").asText() : "CARD");

                // DB 저장 (userId null 가능)
                paymentRepository.savePayment(
                        orderId,
                        userId,
                        amount,
                        paymentResponse.getPaymentKey(),
                        paymentResponse.getPgProvider(),
                        paymentResponse.getPaymentMethod()
                );

                // 이벤트 발행
                paymentEventPublisher.publishPaymentCompletedEvent(orderId, userId, amount);

                log.info("Payment confirmed: orderId={}, userId={}", orderId, userId);
                return paymentResponse;

            } else {
                paymentResponse.setStatus("FAIL: Toss confirm failed");
                log.warn("Toss confirm failed for orderId={}", orderId);
                return paymentResponse;
            }

        } catch (PaymentException pe) {
            throw pe;
        } catch (Exception e) {
            log.error("Payment processing failed for orderId={}", request.getOrderId(), e);
            throw new PaymentException("Payment processing failed: " + e.getMessage());
        }
    }

    /**
     * 이벤트 처리용 메서드
     * PaymentEventHandler에서 호출 가능
     */
    public void processPaymentFromEvent(String orderId, BigDecimal amount, JsonNode payload) throws PaymentException {
        PaymentConfirmRequest request = new PaymentConfirmRequest();
        request.setOrderId(orderId);
        request.setAmount(amount);
        if (payload != null && payload.has("paymentKey")) {
            request.setPaymentKey(payload.get("paymentKey").asText());
        }

        confirmPayment(request, null);
    }

    public static class PaymentException extends Exception {
        public PaymentException(String message) {
            super(message);
        }
    }
}
