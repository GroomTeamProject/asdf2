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

    @Transactional
    
    public Payment completePayment(String paymentKey, String pgProvider, String pgTid, BigDecimal amount) {
        // paymentKey로 결제 조회
        Payment payment = paymentRepository.findByPaymentKey(paymentKey)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found: " + paymentKey));

        // 결제 금액 설정 (Order에서 가져오기)
        Order order = payment.getOrder();
        BigDecimal totalAmount = order.getTotalAmount();
        payment.setAmount(totalAmount);

        // 결제 승인 처리
        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setPgProvider(pgProvider);
        payment.setPgTid(pgTid);
        payment.setAmount(amount);

        return paymentRepository.save(payment);
    }
}
