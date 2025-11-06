package io.goorm.team02.payment.handler;

import com.fasterxml.jackson.databind.JsonNode;
import io.goorm.team02.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;


@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventHandler {

    private final PaymentService paymentService;

    @KafkaListener(
            topics = "${order.events.topic}",
            groupId = "payment-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleOrderEvent(JsonNode eventJson) {
        try {
            log.info("[Kafka] received event: {}", eventJson);

            String eventType = eventJson.path("eventType").asText("");
            if (!"ORDER_CREATED".equals(eventType)) {
                log.info("ignore eventType={}", eventType);
                return;
            }

            JsonNode payload = eventJson.path("order");
            if (payload.isMissingNode()) {
                log.warn("order payload missing in event, skip: {}", eventJson);
                return;
            }

            String orderId = payload.path("orderNumber").asText("");
            int totalAmount = payload.path("totalAmount").asInt(0);
            String userId = payload.path("userId").asText("");

            if (orderId.isEmpty()) {
                log.warn("orderNumber not found in payload, skip: {}", payload);
                return;
            }

            paymentService.processPaymentFromEvent(orderId, BigDecimal.valueOf(totalAmount), payload);
            log.info("[Kafka] dispatched payment processing for orderId={}, amount={}", orderId, totalAmount);

        } catch (Exception ex) {
            log.error("[Kafka] failed to handle event", ex);
        }
    }
}
