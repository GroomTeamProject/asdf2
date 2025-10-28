package io.goorm.team02.payment.service;

import io.goorm.team02.payment.client.PaymentServiceClient;
import io.goorm.team02.payment.entity.Payment;
import io.goorm.team02.payment.entity.PaymentTransaction;
import io.goorm.team02.payment.entity.enums.PaymentStatus;
import io.goorm.team02.payment.dto.PaymentConfirmRequest;
import io.goorm.team02.payment.dto.PaymentResponse;
import io.goorm.team02.payment.event.PaymentEvent;
import io.goorm.team02.payment.event.PaymentEventPublisher;
import io.goorm.team02.payment.repository.PaymentRepository;
import io.goorm.team02.payment.repository.PaymentTransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Map;
import java.math.BigDecimal;

@Service
public class PaymentService {

    private final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final WebClient webClient;
    private final PaymentRepository paymentRepository;
    private final PaymentTransactionRepository transactionRepository;
    private final PaymentServiceClient paymentServiceClient;
    private final PaymentEventPublisher eventPublisher;  // Kafka + SSE 연동

    @Value("${toss.secret-key}")
    private String secretKey;

    public PaymentService(WebClient.Builder webClientBuilder,
                          PaymentRepository paymentRepository,
                          PaymentTransactionRepository transactionRepository,
                          PaymentServiceClient paymentServiceClient,
                          PaymentEventPublisher eventPublisher) {
        this.webClient = webClientBuilder.build();
        this.paymentRepository = paymentRepository;
        this.transactionRepository = transactionRepository;
        this.paymentServiceClient = paymentServiceClient;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public PaymentResponse confirmPayment(PaymentConfirmRequest request) {

        log.info("Payment request received: orderId={}, amount={}", request.getOrderId(), request.getAmount());

        // --- 1) 주문 상태 확인 ---
        var orderResponse = paymentServiceClient.getOrderEvent(request.getOrderId());
        if (!"CREATED".equals(orderResponse.getStatus())) {
            throw new PaymentException("결제를 진행할 수 없는 주문 상태입니다.");
        }

        // --- 2) 중복 결제 방지 ---
        if (paymentRepository.existsByPaymentKey(request.getPaymentKey())) {
            throw new PaymentException("이미 결제 완료된 주문입니다.");
        }

        // --- 3) Payment 엔티티 생성 ---
        Payment payment = new Payment();
        Long orderIdLong = request.getOrderId() != null ? Long.valueOf(request.getOrderId()) : null;
        Long userIdLong = request.getUserId() != null ? Long.valueOf(request.getUserId()) : null;

        payment.setOrderId(orderIdLong);
        payment.setUserId(userIdLong);
        payment.setAmount(request.getAmount());
        payment.setPaymentKey(request.getPaymentKey());
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setPgProvider(request.getPgProvider());
        payment.setStatus(PaymentStatus.PENDING);
        payment.setCreatedAt(LocalDateTime.now());
        paymentRepository.save(payment);

        // --- 4) PaymentTransaction 기록 ---
        PaymentTransaction tx = new PaymentTransaction();
        tx.setPayment(payment);
        tx.setType("ATTEMPT");
        tx.setStatus(PaymentStatus.PENDING.name());
        tx.setAmount(payment.getAmount() != null ? payment.getAmount().intValue() : 0);
        tx.setCreatedAt(LocalDateTime.now());
        transactionRepository.save(tx);

        // --- 5) Toss 결제 API 호출 ---
        String url = "https://api.tosspayments.com/v1/payments/confirm";
        String encodedKey = Base64.getEncoder().encodeToString((secretKey + ":").getBytes());
        Map<String, Object> body = Map.of(
                "paymentKey", request.getPaymentKey(),
                "orderId", request.getOrderId(),
                "amount", request.getAmount()
        );

        PaymentResponse paymentResponse;
        try {
            paymentResponse = webClient.post()
                    .uri(url)
                    .header(HttpHeaders.AUTHORIZATION, "Basic " + encodedKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(PaymentResponse.class)
                    .block();
        } catch (Exception e) {
            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);

            tx.setType("FAIL");
            tx.setStatus(PaymentStatus.FAILED.name());
            tx.setUpdatedAt(LocalDateTime.now());
            transactionRepository.save(tx);

            publishEvent(payment, false); // 실패 이벤트 발행
            log.error("결제 API 호출 실패: orderId={}", request.getOrderId(), e);
            throw new PaymentException("결제 실패: " + e.getMessage());
        }

        // --- 6) 결제 성공 처리 ---
        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setUpdatedAt(LocalDateTime.now());
        paymentRepository.save(payment);

        tx.setType("APPROVAL");
        tx.setStatus(PaymentStatus.COMPLETED.name());
        tx.setUpdatedAt(LocalDateTime.now());
        transactionRepository.save(tx);

        publishEvent(payment, true); // 성공 이벤트 발행

        log.info("Payment completed: orderId={}, amount={}", payment.getOrderId(), payment.getAmount());

        return paymentResponse;
    }

    /**
     * Kafka + SSE 이벤트 발행
     * @param payment 결제 엔티티
     * @param success 성공 여부
     */
    private void publishEvent(Payment payment, boolean success) {
        PaymentEvent event = new PaymentEvent();
        event.setPaymentKey(payment.getPaymentKey());
        event.setOrderId(payment.getOrderId() != null ? String.valueOf(payment.getOrderId()) : null);
        event.setAmount(payment.getAmount());
        event.setMethod(payment.getPaymentMethod());
        event.setStatus(payment.getStatus() != null ? payment.getStatus().name() : null);
        event.setPgProvider(payment.getPgProvider());

        if (success) {
            eventPublisher.publishPaymentCompleted(event);
        } else {
            eventPublisher.publishPaymentFailed(event);
        }
    }

    // --- Custom Exception ---
    public static class PaymentException extends RuntimeException {
        public PaymentException(String message) { super(message); }
    }
}
