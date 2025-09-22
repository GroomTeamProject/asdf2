package io.goorm.team02.core.payments.service;

import io.goorm.team02.core.orders.domain.Order;
import io.goorm.team02.core.orders.repository.OrderRepository;
import io.goorm.team02.core.payments.domain.Payment;
import io.goorm.team02.core.payments.domain.enums.PaymentStatus;
import io.goorm.team02.core.payments.repository.PaymentRepository;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    public PaymentService(PaymentRepository paymentRepository, OrderRepository orderRepository) {
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
    }

    // 결제 완료
    @Transactional
    public Payment completePayment(Order order, String paymentKey, String pgProvider, String pgTid, BigDecimal amount) {
        if (order == null) {
            throw new IllegalArgumentException("주문 정보가 없습니다.");
        }

        if (paymentKey == null || paymentKey.isEmpty()) {
            throw new IllegalArgumentException("결제 키가 없습니다.");
        }

        Payment payment = paymentRepository.findByPaymentKey(paymentKey)
                .orElseGet(() -> {
                    Payment newPayment = new Payment();
                    newPayment.setPaymentKey(paymentKey);
                    newPayment.setOrder(order);
                    return newPayment;
                });

        // 금액 검증
        if (amount == null || amount.compareTo(order.getTotalAmount()) != 0) {
            throw new IllegalArgumentException("결제 금액 불일치");
        }

        payment.setAmount(amount);
        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setPgProvider(pgProvider != null ? pgProvider : "unknown");
        payment.setPgTid(pgTid != null ? pgTid : "");

        return paymentRepository.save(payment);
    }

    // 결제 상태 변경
    @Transactional
    public Payment updatePaymentStatus(Long paymentId, PaymentStatus newStatus) {
        if (paymentId == null) {
            throw new IllegalArgumentException("결제 ID가 없습니다.");
        }

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("해당 결제를 찾을 수 없음"));

        if (payment.getStatus() == PaymentStatus.CANCELLED || payment.getStatus() == PaymentStatus.REFUNDED) {
            throw new IllegalStateException("이미 종료된 결제는 상태 변경 불가");
        }

        payment.setStatus(newStatus != null ? newStatus : payment.getStatus());
        return paymentRepository.save(payment);
    }

    public void cancelPayment(String paymentKey, BigDecimal amount) {

        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth("test_gsk_docs_OaPz8L5KdmQXkzRz3y47BMw6", "");
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();
        body.put("cancelAmount", amount.longValue());
        body.put("cancelReason", "사용자 주문 취소");
        body.put("taxFreeAmount", 0L);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.postForEntity(
                "https://api.tosspayments.com/v1/payments/" + paymentKey + "/cancel",
                request,
                String.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Toss 결제 취소 실패");
        }
    }

}
