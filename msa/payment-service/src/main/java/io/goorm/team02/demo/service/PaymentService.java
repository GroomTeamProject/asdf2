package io.goorm.team02.payment.service;

import io.goorm.team02.payment.client.PaymentServiceClient;
import io.goorm.team02.payment.domain.Payment;
import io.goorm.team02.payment.domain.PaymentTransaction;
import io.goorm.team02.payment.domain.enums.PaymentStatus;
import io.goorm.team02.payment.dto.PaymentConfirmRequest;
import io.goorm.team02.payment.dto.PaymentResponse;
import io.goorm.team02.payment.dto.EventResponse;
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

@Service
public class PaymentService {

    private final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final WebClient webClient;
    private final PaymentRepository paymentRepository;
    private final PaymentTransactionRepository transactionRepository;
    private final PaymentServiceClient paymentServiceClient;
    private final PaymentEventPublisher eventPublisher;

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

        // 주문 상태 확인 (API 호출)
        var orderResponse = paymentServiceClient.getOrderEvent(request.getOrderId());
        if (!"CREATED".equals(orderResponse.getStatus())) {
            throw new PaymentException("결제를 진행할 수 없는 주문 상태입니다.");
        }

        // 중복 결제 방지
        if (paymentRepository.existsByPaymentKey(request.getPaymentKey())) {
            throw new PaymentException("이미 결제 완료된 주문입니다.");
        }

        // Payment 엔티티 생성 및 저장
        Payment payment = new Payment();
        payment.setOrderId(request.getOrderId());
        payment.setUserId(request.getUserId());
        payment.setAmount(request.getAmount());
        payment.setPaymentKey(request.getPaymentKey());
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setPgProvider(request.getPgProvider());
        payment.setStatus(PaymentStatus.PENDING);
        payment.setCreatedAt(LocalDateTime.now());
        paymentRepository.save(payment);

        // PaymentTransaction 기록 (결제 시도)
        PaymentTransaction tx = new PaymentTransaction();
        tx.setPayment(payment);
        tx.setType("ATTEMPT");
        tx.setStatus(PaymentStatus.PENDING.name());
        tx.setAmount(payment.getAmount());
        tx.setCreatedAt(LocalDateTime.now());
        transactionRepository.save(tx);

        // Toss 결제 API 호출
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

            // Transaction 실패 기록
            tx.setType("FAIL");
            tx.setStatus(PaymentStatus.FAILED.name());
            tx.setUpdatedAt(LocalDateTime.now());
            transactionRepository.save(tx);

            publishFailedEvent(payment);
            log.error("결제 API 호출 실패: orderId={}", request.getOrderId(), e);
            throw new PaymentException("결제 실패: " + e.getMessage());
        }

        // 5. 결제 성공 처리
        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setUpdatedAt(LocalDateTime.now());
        paymentRepository.save(payment);

        // Transaction 성공 기록
        tx.setType("APPROVAL");
        tx.setStatus(PaymentStatus.COMPLETED.name());
        tx.setUpdatedAt(LocalDateTime.now());
        transactionRepository.save(tx);

        publishCompletedEvent(payment);

        log.info("Payment completed: orderId={}, amount={}", payment.getOrderId(), payment.getAmount());

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

    // Custom Exception
    public static class PaymentException extends RuntimeException {
        public PaymentException(String message) { super(message); }
    }
}
