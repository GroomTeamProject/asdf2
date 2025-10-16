package io.goorm.team02.common.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

public class JwtUtils {

    /**
     * JWT payload에서 userId 추출
     */
    public static Long getUserIdFromToken(String token, String secret) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(secret.getBytes())
                .build()
                .parseClaimsJws(token)
                .getBody();
        return ((Number) claims.get("userId")).longValue();
    }

    /**
     * JWT payload에서 email 추출
     */
    public static String getEmailFromToken(String token, String secret) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(secret.getBytes())
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }

    /**
     * 단순 JWT 검증 (auth-service secret 필요)
     */
    public static boolean validateToken(String token, String secret) {
        try {
            Jwts.parserBuilder().setSigningKey(secret.getBytes()).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}

