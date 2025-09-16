package io.goorm.team02.core.sse.service;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class SSEConnectionService {

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
     * 사용자 연결 조회
     * 
     * @param userId 사용자 ID
     * @return SseEmitter
     */
    public SseEmitter getConnection(Long userId) {
        return userConnections.get(userId);
    }

    /**
     * 사용자 연결 제거
     * 
     * @param userId 사용자 ID
     */
    public void removeConnection(Long userId) {
        userConnections.remove(userId);
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
