package io.goorm.team02.payment.event;

import org.springframework.stereotype.Component;
import java.math.BigDecimal;

@Component
public class PaymentEventPublisher {

    public void publishPaymentCompletedEvent(String orderId, String userId, BigDecimal amount) {
        // 실제 Kafka 전송 로직 대신 로그 출력
        System.out.println("Payment completed event published: orderId=" + orderId + ", userId=" + userId + ", amount=" + amount);
    }
}
