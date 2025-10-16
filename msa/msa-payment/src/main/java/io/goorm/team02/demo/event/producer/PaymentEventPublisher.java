package io.goorm.team02.payment.event;

import io.goorm.team02.payment.dto.EventResponse;
import org.springframework.stereotype.Component;

@Component
public class PaymentEventPublisher {

    public void publishPaymentCompleted(EventResponse event) {
        // Kafka, RabbitMQ 등 이벤트 발행 로직
        System.out.println("Payment Completed Event 발행: " + event);
    }

    public void publishPaymentFailed(EventResponse event) {
        System.out.println("Payment Failed Event 발행: " + event);
    }
}
