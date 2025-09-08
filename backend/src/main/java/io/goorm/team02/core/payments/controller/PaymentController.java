// src/main/java/com/example/toss/controller/PaymentController.java
package io.goorm.team02.core.payments.controller;

import io.goorm.team02.core.payments.dto.PaymentRequest;
import io.goorm.team02.core.payments.dto.PaymentResponse;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final String SECRET_KEY = "test_sk_xxxxxxxxxxxxx"; // TossPayments 테스트 시크릿 키

    @PostMapping("/create")
    public PaymentResponse createPayment(@RequestBody PaymentRequest request) {

        String url = "https://api.tosspayments.com/v1/payments";

        RestTemplate restTemplate = new RestTemplate();

        Map<String, Object> body = new HashMap<>();
        body.put("orderId", request.getOrderId());
        body.put("orderName", request.getOrderName());
        body.put("amount", request.getAmount());
        body.put("customerEmail", "customer123@gmail.com");
        body.put("successUrl", "http://localhost:5173/success");
        body.put("failUrl", "http://localhost:5173/fail");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBasicAuth(SECRET_KEY, ""); // TossPayments는 Basic Auth 사용

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);

        Map data = response.getBody();
        String paymentUrl = (String) data.get("paymentUrl"); // TossPayments가 반환하는 결제 페이지 URL

        return new PaymentResponse(paymentUrl);
    }
}
