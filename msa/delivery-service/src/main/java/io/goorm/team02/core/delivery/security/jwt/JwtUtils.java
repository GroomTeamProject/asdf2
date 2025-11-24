package io.goorm.team02.core.delivery.security.jwt;
import io.goorm.team02.common.exception.errors.UnauthorizedException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;

@Slf4j
@Component
public class JwtUtils {

    @Value("${jwt.secret}")
    private String secret;

    private Key key;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
    }

    /** 토큰 검증 */
    public Claims parse(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /** Authorization 헤더에서 순수 JWT 추출 */
    public String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            throw new UnauthorizedException("토큰 없음");
        }
        return header.substring(7);
    }

    /** 사용자 ID 추출 */
    public Long getUserId(Claims claims) {
        return claims.get("userId", Long.class);
    }

    /** 이메일 추출 */
    public String getEmail(Claims claims) {
        return claims.getSubject();
    }
}
