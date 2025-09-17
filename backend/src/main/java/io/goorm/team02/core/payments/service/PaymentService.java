package io.goorm.team02.core.payments.service;

import io.goorm.team02.core.orders.domain.Order;
import io.goorm.team02.core.orders.repository.OrderRepository;
import io.goorm.team02.core.payments.domain.Payment;
import io.goorm.team02.core.payments.domain.enums.PaymentStatus;
import io.goorm.team02.core.payments.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    public PaymentService(PaymentRepository paymentRepository, OrderRepository orderRepository) {
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
    }

    // PaymentService
    @Transactional
    public Payment completePayment(Order order, String paymentKey, String pgProvider, String pgTid, BigDecimal amount) {

        Payment payment = paymentRepository.findByPaymentKey(paymentKey)
                .orElseGet(() -> {
                    Payment newPayment = new Payment();
                    newPayment.setPaymentKey(paymentKey);
                    newPayment.setOrder(order);
                    return newPayment;
                });

        // 금액 검증
        if (amount.compareTo(order.getTotalAmount()) != 0) {
            throw new IllegalArgumentException("결제 금액 불일치");
        }

        payment.setAmount(amount);
        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setPgProvider(pgProvider);
        payment.setPgTid(pgTid);

        return paymentRepository.save(payment);
    }

}
