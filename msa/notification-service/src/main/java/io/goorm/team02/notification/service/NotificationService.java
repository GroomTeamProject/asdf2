package io.goorm.team02.notification.service;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    public void handleOrderEvent(String eventType, String message) {
        switch (eventType) {
            case "ORDER_CREATED":
                processOrderCreatedEvent(message);
                break;
            case "ORDER_ACCEPTED":
                processOrderAcceptedEvent(message);
                break;
            case "ORDER_COOKING":
                processOrderCookingEvent(message);
                break;
            case "ORDER_READY":
                processOrderReadyEvent(message);
                break;
            case "ORDER_PICKED_UP":
                processOrderPickedUpEvent(message);
                break;
            case "ORDER_DELIVERED":
                processOrderDeliveredEvent(message);
                break;
            case "ORDER_CANCELLED":
                processOrderCancelledEvent(message);
                break;
            case "ORDER_REJECTED":
                processOrderRejectedEvent(message);
                break;
            default:
                log.warn("Unknown event type: {}", eventType);
                break;
        }
    }

    private void processOrderCreatedEvent(String message) {
        log.info("Processing order created event: {}", message);
        // 여기에 실제 비즈니스 로직을 구현
    }

    private void processOrderAcceptedEvent(String message) {
        log.info("Processing order accepted event: {}", message);
        // 여기에 실제 비즈니스 로직을 구현
    }

    private void processOrderCookingEvent(String message) {
        log.info("Processing order cooking event: {}", message);
        // 여기에 실제 비즈니스 로직을 구현
    }

    private void processOrderReadyEvent(String message) {
        log.info("Processing order ready event: {}", message);
        // 여기에 실제 비즈니스 로직을 구현
    }

    private void processOrderPickedUpEvent(String message) {
        log.info("Processing order picked up event: {}", message);
        // 여기에 실제 비즈니스 로직을 구현
    }

    private void processOrderDeliveredEvent(String message) {
        log.info("Processing order delivered event: {}", message);
        // 여기에 실제 비즈니스 로직을 구현
    }

    private void processOrderCancelledEvent(String message) {
        log.info("Processing order cancelled event: {}", message);
        // 여기에 실제 비즈니스 로직을 구현
    }

    private void processOrderRejectedEvent(String message) {
        log.info("Processing order rejected event: {}", message);
        // 여기에 실제 비즈니스 로직을 구현
    }
}
