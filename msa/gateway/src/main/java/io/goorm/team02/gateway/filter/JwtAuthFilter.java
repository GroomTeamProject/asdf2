package io.goorm.team02.gateway.filter;

import io.goorm.team02.gateway.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;  // ✅ 로그 추가
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j   // ✅ Lombok 로그 어노테이션
@Component
@RequiredArgsConstructor
public class JwtAuthFilter implements GlobalFilter, Ordered {

    private final JwtUtils jwtUtils;
    private final RedisTemplate<String, Object> redisTemplate;
    private static final String BLACKLIST_PREFIX = "blacklist:";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, org.springframework.cloud.gateway.filter.GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().value();
        log.info("🔹 Incoming request: {}", path);

        // 1️⃣ 로그인/회원가입 등 공개 경로는 인증 제외
        if (path.startsWith("/api/auth") || path.startsWith("/api/public")) {
            log.debug("🟢 Public path, skipping authentication: {}", path);
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("🚫 Missing or invalid Authorization header for path: {}", path);
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String token = authHeader.substring(7);
        log.debug("🔸 Extracted JWT token: {}", token.substring(0, Math.min(20, token.length())) + "...");

        // 2️⃣ 블랙리스트 확인
        if (Boolean.TRUE.equals(redisTemplate.hasKey(BLACKLIST_PREFIX + token))) {
            log.warn("🚫 Token is blacklisted (path: {}, token prefix: {})", path, token.substring(0, 15));
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        // 3️⃣ JWT 유효성 검증
        if (!jwtUtils.validateToken(token)) {
            log.warn("🚫 Invalid or expired JWT token (path: {}, token prefix: {})", path, token.substring(0, 15));
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        // 4️⃣ JWT에서 사용자 정보 추출 후 헤더 추가
        Long userId = jwtUtils.getUserIdFromToken(token);
        String email = jwtUtils.getEmailFromToken(token);
        log.info("✅ Authenticated user -> ID: {}, Email: {}", userId, email);

        ServerHttpRequest mutated = exchange.getRequest().mutate()
                .header("X-User-Id", String.valueOf(userId))
                .header("X-User-Email", email)
                .build();

        return chain.filter(exchange.mutate().request(mutated).build());
    }

    // 필터 우선순위 (낮을수록 먼저 실행)
    @Override
    public int getOrder() {
        return -1;
    }
}
