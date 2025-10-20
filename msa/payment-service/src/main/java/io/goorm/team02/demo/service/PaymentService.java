package io.goorm.team02.payment.service;

import io.goorm.team02.payment.client.PaymentServiceClient;
import io.goorm.team02.payment.domain.Payment;
import io.goorm.team02.payment.domain.enums.PaymentStatus;
import io.goorm.team02.payment.dto.PaymentConfirmRequest;
import io.goorm.team02.payment.dto.PaymentResponse;
import io.goorm.team02.payment.dto.EventResponse;
import io.goorm.team02.payment.event.PaymentEventPublisher;
import io.goorm.team02.payment.repository.PaymentRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.beans.factory.annotation.Value;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Map;

@Service
public class PaymentService {

    private final RestTemplate restTemplate;
    private final PaymentRepository paymentRepository;
    private final PaymentServiceClient paymentServiceClient;
    private final PaymentEventPublisher eventPublisher;

    @Value("${toss.secret-key}")
    private String secretKey;

    public PaymentService(RestTemplateBuilder builder,
                          PaymentRepository paymentRepository,
                          PaymentServiceClient paymentServiceClient,
                          PaymentEventPublisher eventPublisher) {
        this.restTemplate = builder.build();
        this.paymentRepository = paymentRepository;
        this.paymentServiceClient = paymentServiceClient;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public PaymentResponse confirmPayment(PaymentConfirmRequest request) {

        // 1. 주문 이벤트 조회
        var orderEvent = paymentServiceClient.getOrderEvent(request.getOrderId());

        if (!"CREATED".equals(orderEvent.getStatus())) {
            throw new RuntimeException("결제를 진행할 수 없는 주문 상태입니다.");
        }

        // 2. 결제 진행 상태 체크
        boolean alreadyAccepted = paymentRepository.existsByOrderIdAndStatus(
                request.getOrderId(), PaymentStatus.COMPLETED);
        if (alreadyAccepted) {
            throw new RuntimeException("이미 결제 완료된 주문입니다.");
        }

        boolean inProgress = paymentRepository.existsByOrderIdAndStatus(
                request.getOrderId(), PaymentStatus.PENDING);
        if (inProgress) {
            throw new RuntimeException("결제가 이미 진행 중입니다. 잠시 후 다시 시도하세요.");
        }

        // 3. Payment 생성
        Payment payment = new Payment();
        payment.setOrderId(request.getOrderId());
        payment.setAmount(request.getAmount());
        payment.setPaymentKey(request.getPaymentKey());
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setPgProvider(request.getPgProvider());
        payment.setStatus(PaymentStatus.PENDING);
        payment.setCreatedAt(LocalDateTime.now());
        paymentRepository.save(payment);

        // 4. Toss 결제 API 호출
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
            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);
            publishFailedEvent(payment);
            throw new RuntimeException("결제 승인 실패: " + e.getResponseBodyAsString());
        } catch (RestClientException e) {
            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);
            publishFailedEvent(payment);
            throw new RuntimeException("결제 API 호출 실패", e);
        }

        // 5. 결제 성공 처리
        payment.setStatus(PaymentStatus.COMPLETED);
        paymentRepository.save(payment);
        publishCompletedEvent(payment);

        return paymentResponse;
    }

    private void publishCompletedEvent(Payment payment) {
        EventResponse event = new EventResponse();
        event.setPaymentKey(payment.getPaymentKey());
        event.setOrderId(payment.getOrderId());
        event.setAmount(payment.getAmount());
        event.setMethod(payment.getPaymentMethod());
        event.setStatus(payment.getStatus().name());
        event.setPgProvider(payment.getPgProvider());
        eventPublisher.publishPaymentCompleted(event);
    }

    private void publishFailedEvent(Payment payment) {
        EventResponse event = new EventResponse();
        event.setPaymentKey(payment.getPaymentKey());
        event.setOrderId(payment.getOrderId());
        event.setAmount(payment.getAmount());
        event.setMethod(payment.getPaymentMethod());
        event.setStatus(payment.getStatus().name());
        event.setPgProvider(payment.getPgProvider());
        eventPublisher.publishPaymentFailed(event);
    }
}
