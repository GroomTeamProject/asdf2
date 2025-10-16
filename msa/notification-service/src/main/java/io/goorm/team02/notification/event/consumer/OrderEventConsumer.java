package io.goorm.team02.notification.event.consumer;

import io.goorm.team02.kafka.client.BaseEventSubscriber;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import io.goorm.team02.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;

/**
 * 주문 이벤트 Consumer
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class OrderEventConsumer extends BaseEventSubscriber {

    private final NotificationService notificationService;

    @KafkaListener(topics = "order-events", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeOrderEvent(String message) {
        log.info("Received order event: {}", message);

        try {
            // JSON에서 eventType 추출
            String eventType = extractEventType(message);
            notificationService.handleOrderEvent(eventType, message);
        } catch (Exception e) {
            log.error("Failed to process order event: {}", message, e);
        }
    }

    private String extractEventType(String message) {
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode jsonNode = mapper.readTree(message);
            return jsonNode.get("eventType").asText();
        } catch (Exception e) {
            log.error("Failed to extract eventType from message: {}", message, e);
            return "UNKNOWN";
        }
    }
}
