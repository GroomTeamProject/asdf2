package io.goorm.team02.payment.handler;

import io.goorm.team02.dto.orders.OrderRequest;
import io.goorm.team02.payment.event.PaymentCompletedEvent;
import io.goorm.team02.payment.service.PaymentService;
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

    /**
     * OrderEvent 수신 후 결제 처리
     * OrderCreatedEvent에서 orderId와 amount를 전달받는다고 가정
     */
    @KafkaListener(topics = "order-events", groupId = "payment-service-group")
    public void handleOrderEvent(OrderRequest orderRequest) {
        try {
            // 주문 ID와 총 금액 계산
            Long orderId = orderRequest.storeId(); // 예시로 storeId 사용, 실제 orderId가 필요하면 event에 포함해야 함
            Integer amount = orderRequest.orderItems().stream()
                    .mapToInt(item -> item.quantity())
                    .sum(); // 총 수량을 금액처럼 사용 (실제 금액 계산 로직에 맞게 수정)

            paymentService.processPaymentFromEvent(orderId, amount, null);

            PaymentCompletedEvent completedEvent = PaymentCompletedEvent.builder()
                    .paymentKey("PAY_" + orderId)
                    .orderId(String.valueOf(orderId))
                    .amount(Long.valueOf(amount))
                    .build();

            kafkaTemplate.send("payment-events", completedEvent.getOrderId(), completedEvent);
            log.info("[PaymentEventHandler] 결제 완료 이벤트 발행: {}", completedEvent.getOrderId());

        } catch (Exception e) {
            log.warn("[PaymentEventHandler] 결제 실패: {}", orderRequest.storeId(), e);
        }
    }

}
