package io.goorm.team02.core.payments.service;

import io.goorm.team02.core.orders.domain.Order;
import io.goorm.team02.core.orders.repository.OrderRepository;
import io.goorm.team02.core.payments.domain.Payment;
import io.goorm.team02.core.payments.domain.enums.PaymentStatus;
import io.goorm.team02.core.payments.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

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

        // 이미 종료된 결제 건 상태 변경 불가
        if (payment.getStatus() == PaymentStatus.CANCELLED || payment.getStatus() == PaymentStatus.REFUNDED) {
            throw new IllegalStateException("이미 종료된 결제는 상태 변경 불가");
        }

        payment.setStatus(newStatus != null ? newStatus : payment.getStatus());
        return paymentRepository.save(payment);
    }

    // 결제 취소
    @Transactional
    public Payment cancelPayment(Long paymentId) {
        if (paymentId == null) {
            throw new IllegalArgumentException("결제 ID가 없습니다.");
        }

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("해당 결제를 찾을 수 없음"));

        if (payment.getStatus() != PaymentStatus.COMPLETED) {
            throw new IllegalStateException("결제 완료 상태가 아니면 취소 불가");
        }

        payment.setStatus(PaymentStatus.CANCELLED);
        return paymentRepository.save(payment);
    }
}
