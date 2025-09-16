package io.goorm.team02.core.sse.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SSE 서비스
 * 실시간 알림을 위한 Server-Sent Events 메시지 전송 기능을 제공합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SSEService {

    // 사용자별 SSE 연결 관리
    private final Map<Long, SseEmitter> userConnections = new ConcurrentHashMap<>();

    /**
     * SSE 연결 등록
     * 
     * @param userId 사용자 ID
     * @return SseEmitter
     */
    public SseEmitter createConnection(Long userId) {
        log.info("사용자 {} SSE 연결 요청", userId);

        // TODO: 사용자, 가게 타임아웃 시간 구분
        SseEmitter emitter = new SseEmitter(30 * 60 * 1000L); // 30분 타임아웃
        
        // 연결 완료/타임아웃/에러 시 정리
        emitter.onCompletion(() -> {
            userConnections.remove(userId);
            log.info("사용자 {} SSE 연결 완료", userId);
        });        
        emitter.onTimeout(() -> {
            userConnections.remove(userId);
            log.info("사용자 {} SSE 연결 타임아웃", userId);
        });        
        emitter.onError((ex) -> {
            userConnections.remove(userId);
            log.error("사용자 {} SSE 연결 에러: {}", userId, ex.getMessage());
        });
        
        userConnections.put(userId, emitter);
        log.info("사용자 {} SSE 연결 등록", userId);
        
        return emitter;
    }

    /**
     * 특정 사용자에게 실시간 알림 전송
     * 
     * @param userId 사용자 ID
     * @param notificationMessage 알림 메시지
     */
    public void sendNotificationToUser(Long userId, String notificationMessage, String notificationType) {
        SseEmitter emitter = userConnections.get(userId);
        
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                    .name(notificationType)
                    .data(notificationMessage));
                log.info("사용자 {}에게 SSE 알림 전송 [{}]: {}", userId, notificationType, notificationMessage);
            } catch (IOException e) {
                log.error("사용자 {}에게 SSE 알림 전송 실패 [{}]: {}", userId, notificationType, e.getMessage());
                userConnections.remove(userId);
            }
        } else {
            log.warn("사용자 {}의 SSE 연결이 없습니다", userId);
        }
    }

    /**
     * 특정 사용자에게 기본 알림 전송 (기존 호환성 유지)
     * 
     * @param userId 사용자 ID
     * @param message 알림 메시지
     */
    public void sendNotificationToUser(Long userId, String message) {
        sendNotificationToUser(userId, message,  "notification");
    }

    /**
     * 사용자 연결 상태 확인
     * 
     * @param userId 사용자 ID
     * @return 연결 상태
     */
    public boolean isUserConnected(Long userId) {
        return userConnections.containsKey(userId);
    }

}
