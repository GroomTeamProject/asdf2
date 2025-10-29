package io.goorm.team02.core.payments.service;

import io.goorm.team02.core.orders.domain.Order;
import io.goorm.team02.core.orders.repository.OrderRepository;
import io.goorm.team02.core.payments.domain.Payment;
import io.goorm.team02.core.payments.domain.enums.PaymentStatus;
import io.goorm.team02.core.payments.repository.PaymentRepository;
import io.goorm.team02.core.payments.dto.PaymentConfirmRequest;
import io.goorm.team02.core.payments.dto.PaymentResponse;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.data.domain.Sort;
import org.springframework.beans.factory.annotation.Value;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Map;

@Service
public class PaymentService {

    private final RestTemplate restTemplate;
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    //@Value("${toss.secret-key}")
    private String secretKey="test_gsk_docs_OaPz8L5KdmQXkzRz3y47BMw6";

    public PaymentService(RestTemplateBuilder builder,
                          PaymentRepository paymentRepository,
                          OrderRepository orderRepository) {
        this.restTemplate = builder.build();
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
    }

    @Transactional
    public PaymentResponse confirmPayment(PaymentConfirmRequest request) {
        String url = "https://api.tosspayments.com/v1/payments/confirm";
        String encodedKey = Base64.getEncoder().encodeToString((secretKey + ":").getBytes());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Basic " + encodedKey);

        Map<String, Object> body = Map.of(
                "paymentKey", request.getPaymentKey(),
                "orderId", request.getOrderId(),
                "amount", request.getAmount()
        );

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        PaymentResponse paymentResponse;

        try {
            ResponseEntity<PaymentResponse> response =
                    restTemplate.exchange(url, org.springframework.http.HttpMethod.POST, entity, PaymentResponse.class);
            paymentResponse = response.getBody();

        } catch (HttpClientErrorException e) {
            // 토스 API가 반환한 실제 에러 메시지 확인
            String errorBody = e.getResponseBodyAsString();
            log.error("=== Toss 결제 승인 실패 ===");
            log.error("PaymentKey: {}", request.getPaymentKey());
            log.error("OrderId: {}", request.getOrderId());
            log.error("Amount: {}", request.getAmount());
            log.error("HTTP Status: {}", e.getStatusCode());
            log.error("Error Body: {}", errorBody);
            log.error("=========================");
            throw new RuntimeException("결제 승인 실패: " + errorBody, e);

        } catch (RestClientException e) {
            log.error("=== Toss 결제 API 호출 실패 ===");
            log.error("PaymentKey: {}", request.getPaymentKey());
            log.error("OrderId: {}", request.getOrderId());
            log.error("Amount: {}", request.getAmount());
            log.error("Exception: ", e);
            log.error("===========================");
            throw new RuntimeException("결제 API 호출 실패", e);
        }

        // --- 주문 객체 가져오기 ---
        Order latestOrder = orderRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"))
                .stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("주문을 찾을 수 없습니다"));

        // --- Payment 엔티티 생성 및 저장 ---
        Payment payment = new Payment();
        payment.setOrder(latestOrder);
        payment.setAmount(BigDecimal.valueOf(request.getAmount()));
        payment.setPaymentKey(request.getPaymentKey());
        payment.setPaymentMethod("Toss");
        payment.setPgProvider(request.getPgProvider());
        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setCreatedAt(LocalDateTime.now());

        paymentRepository.save(payment);

        return paymentResponse;
    }
}
