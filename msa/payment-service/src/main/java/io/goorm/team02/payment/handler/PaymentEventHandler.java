package io.goorm.team02.payment.handler;

import io.goorm.team02.dto.order.OrderCreatedEvent;
import io.goorm.team02.dto.payment.PaymentCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentEventHandler {

    private final KafkaTemplate<String, PaymentCompletedEvent> kafkaTemplate;
    private final PaymentService paymentService;

    @KafkaListener(topics = "order-events", groupId = "payment-service-group")
    public void handleOrderEvent(OrderCreatedEvent event) {
        boolean result = paymentService.processPayment(event); // PaymentService에 구현 필요

        if (result) {
            PaymentCompletedEvent completedEvent = PaymentCompletedEvent.builder()
                    .paymentKey("PAY_" + event.getOrderId())
                    .orderId(event.getOrderId())
                    .amount(event.getAmount())
                    .build();

            kafkaTemplate.send("payment-events", completedEvent.getOrderId(), completedEvent);
            log.info("[PaymentEventHandler] 결제 완료 이벤트 발행: {}", completedEvent.getOrderId());
        } else {
            log.warn("[PaymentEventHandler] 결제 실패: {}", event.getOrderId());
        }
    }
}
