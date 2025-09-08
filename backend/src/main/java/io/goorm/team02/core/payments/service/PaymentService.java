package io.goorm.team02.core.payments.service;

import io.goorm.team02.core.orders.domain.Order;
import io.goorm.team02.core.payments.domain.Payment;
import io.goorm.team02.core.payments.domain.enums.PaymentMethod;
import io.goorm.team02.core.payments.domain.enums.PaymentStatus;
import io.goorm.team02.core.payments.repository.PaymentRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;

    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    public Payment createPayment(Order order, String paymentKey,
            PaymentMethod method, BigDecimal amount,
            PaymentStatus status) {
        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setPaymentKey(paymentKey);
        payment.setMethod(method);
        payment.setAmount(amount);
        payment.setStatus(status != null ? status : PaymentStatus.PENDING);
        return paymentRepository.save(payment);
    }
}
