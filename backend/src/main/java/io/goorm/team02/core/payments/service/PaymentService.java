// src/main/java/io/goorm/team02/core/payments/service/PaymentService.java
package io.goorm.team02.core.payments.service;

import io.goorm.team02.core.payments.dto.PaymentRequest;
import io.goorm.team02.core.payments.dto.PaymentResponse;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {

    public PaymentResponse createPayment(PaymentRequest request) {
        // 실제로는 Toss API 호출 또는 DB 저장
        // 여기서는 예제용으로 간단히 mock 데이터 반환
        String paymentId = "pay-" + System.currentTimeMillis();
        String status = "READY"; // 결제 대기 상태

        return new PaymentResponse(paymentId, request.getAmount(), status);
    }
}
