package io.goorm.team02.core.users.service;

import io.goorm.team02.core.sse.service.SSEService;
import io.goorm.team02.core.orders.event.OrderAcceptedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 사용자 알림 서비스
 * 주문 생성 및 상태 변경 시 사용자에게 알림을 보냅니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserNotificationService {

    private final SSEService sseService;

    /**
     * 주문 수락 이벤트 리스너
     * 주문이 수락되면 사용자에게 주문 확인 알림을 보냅니다.
     */
    @EventListener
    @Async("eventTaskExecutor")
    public void handleOrderAccepted(OrderAcceptedEvent event) {
        long userId = event.getCustomerId();
        String message = event.getCustomerMessage();

        // 1. 실시간 알림 전송
        sendRealTimeNotification(userId, message);
    }

    /**
     * 실시간 알림 전송 (SSE)
     */
    private void sendRealTimeNotification(Long userId, String message) {
        sseService.sendNotificationToUser(userId, message);
    }

}
