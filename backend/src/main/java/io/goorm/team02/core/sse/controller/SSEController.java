package io.goorm.team02.core.sse.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import io.goorm.team02.core.sse.service.SSEService;

/**
 * SSE 컨트롤러
 * 실시간 알림을 위한 Server-Sent Events 엔드포인트를 제공합니다.
 */
@Slf4j
@RestController
@RequestMapping("/api/sse")
@RequiredArgsConstructor
public class SSEController {

    private final SSEService sseService;

    /**
     * 사용자 SSE 연결 생성
     * 
     * @param userId 사용자 ID
     * @return SseEmitter
     */
    @GetMapping(value = "/connect/{userId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter connect(@PathVariable Long userId) {
        log.info("사용자 {} SSE 연결 요청", userId);
        return sseService.createConnection(userId);
    }

    /**
     * 연결 상태 확인
     * 
     * @param userId 사용자 ID
     * @return 연결 상태
     */
    @GetMapping("/status/{userId}")
    public boolean getConnectionStatus(@PathVariable Long userId) {
        return sseService.isUserConnected(userId);
    }
}
