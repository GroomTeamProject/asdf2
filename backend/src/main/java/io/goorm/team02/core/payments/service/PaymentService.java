package io.goorm.team02.core.payments.service;

import io.goorm.team02.core.payments.domain.enums.PaymentStatus;
import io.goorm.team02.core.payments.controller.PaymentController.PaymentRequest;
import io.goorm.team02.core.payments.domain.Payment;
import io.goorm.team02.core.payments.repository.PaymentRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;

    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    // 결제 생성
    public Payment createPayment(Long orderId, String paymentKey,
                                 String pgProvider, String pgTid,
                                 PaymentStatus status,
                                 BigDecimal amount,
                                 io.goorm.team02.core.payments.domain.enums.PaymentMethod method) {
        Payment payment = new Payment();
        payment.setOrderId(orderId);
        payment.setPaymentKey(paymentKey);
        payment.setPgProvider(pgProvider);
        payment.setPgTid(pgTid);
        payment.setStatus(status != null ? status : PaymentStatus.PENDING);
        payment.setAmount(amount);
        payment.setMethod(method);
        return paymentRepository.save(payment);
    }

    // 결제 조회
    public Optional<Payment> getPayment(PaymentRequest paymentRequest) {
        return paymentRepository.findById(paymentRequest);
    }

    // 결제 상태 변경 (예: 취소)
    public Payment updateStatus(Long id, PaymentStatus newStatus, String failedReason) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
        payment.setStatus(newStatus);
        if(failedReason != null) payment.setFailedReason(failedReason);
        return paymentRepository.save(payment);
    }

    public Payment createPayment(PaymentRequest paymentRequest) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'createPayment'");
    }
}
