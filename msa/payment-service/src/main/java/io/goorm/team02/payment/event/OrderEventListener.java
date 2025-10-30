package io.goorm.team02.payment.event;

import com.fasterxml.jackson.databind.JsonNode;
import io.goorm.team02.payment.service.PaymentService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class OrderEventListener {

    private final Logger log = LoggerFactory.getLogger(OrderEventListener.class);

    private final PaymentService paymentService;

    public OrderEventListener(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /**
     * Kafka topic 'order-events' 구독
     * 주문이 생성되면 자동으로 결제 처리
     * JsonNode로 직접 역직렬화
     */
    @KafkaListener(
            topics = "${order.events.topic}",
            groupId = "team02-backend-group",
            containerFactory = "kafkaListenerContainerFactory" // 새 컨테이너 사용
    )
    public void handleOrderEvent(JsonNode eventNode) {
        try {
            Long orderId = eventNode.has("orderId") ? eventNode.get("orderId").asLong() : null;
            Integer totalAmount = eventNode.has("totalAmount") ? eventNode.get("totalAmount").asInt() : null;

            if (orderId == null || totalAmount == null) {
                log.warn("주문 이벤트 정보 부족: {}", eventNode);
                return;
            }

            log.info("Kafka Order Event 수신: orderId={}, totalAmount={}", orderId, totalAmount);

            // PaymentService에서 주문 이벤트 기반 결제 처리
            paymentService.processPaymentFromEvent(orderId, totalAmount, eventNode);

        } catch (Exception e) {
            log.error("주문 이벤트 처리 실패: eventNode={}", eventNode, e);
        }
    }
}
