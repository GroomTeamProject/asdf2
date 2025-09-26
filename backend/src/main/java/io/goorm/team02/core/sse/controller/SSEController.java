package io.goorm.team02.core.sse.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import io.goorm.team02.core.sse.service.SSEConnectionService;
import io.goorm.team02.core.auth.annotation.CurrentUser;
import io.goorm.team02.core.stores.domain.TempUser;

/**
 * SSE 컨트롤러
 * 실시간 알림을 위한 Server-Sent Events 엔드포인트를 제공합니다.
 */
@Slf4j
@RestController
@RequestMapping("/api/sse")
@RequiredArgsConstructor
public class SSEController {

    private final SSEConnectionService sseConnectionService;

    /**
     * 사용자 SSE 연결 생성 (인증된 사용자만)
     * 
     * @return SseEmitter
     */
    @GetMapping(value = "/connect", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter connect(@CurrentUser TempUser user) {
        return sseConnectionService.createConnection(user.getId());
    }

    /**
     * 연결 상태 확인 (인증된 사용자만)
     * 
     * @return 연결 상태
     */
    @GetMapping("/status")
    public boolean getConnectionStatus(@CurrentUser TempUser user) {
        return sseConnectionService.isUserConnected(user.getId());
    }
}
