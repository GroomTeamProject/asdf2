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
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.beans.factory.annotation.Value;



import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Base64;


@Service
public class PaymentService {

    private final RestTemplate restTemplate;

    @Value("${toss.secret-key}")
    private String secretKey;

    public PaymentService(RestTemplateBuilder builder) {
        this.restTemplate = builder.build();
    }

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

        try {
            ResponseEntity<PaymentResponse> response =
                    restTemplate.exchange(url, HttpMethod.POST, entity, PaymentResponse.class);
            return response.getBody();
        } catch (HttpClientErrorException e) {
            throw new RuntimeException("결제 승인 실패: " + e.getResponseBodyAsString());
        } catch (RestClientException e) {
            throw new RuntimeException("결제 API 호출 실패", e);
        }
    }

}
