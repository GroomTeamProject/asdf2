package io.goorm.team02.users.service;


import io.goorm.team02.users.domain.TokenBlacklist;
import io.goorm.team02.users.repository.TokenBlacklistRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import java.util.concurrent.TimeUnit;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private final TokenBlacklistRepository repository;
    private final RedisTemplate<String, Object> redisTemplate;
    private static final String BLACKLIST_PREFIX = "blacklist:";


    //public TokenBlacklistService(TokenBlacklistRepository repository) {
    //    this.repository = repository;
    //}

    /**
     * 토큰의 jti와 만료시간으로 블랙리스트에 추가
     */
    @Transactional
    public void blacklistToken(String jti, Date expiresAtDate) {
        LocalDateTime expiresAt = LocalDateTime.ofInstant(expiresAtDate.toInstant(), ZoneId.systemDefault());
        System.out.println("[BLACKLIST SERVICE] Saving jti: " + jti + " expiresAt: " + expiresAt);
        
        // db저장
        TokenBlacklist entry = new TokenBlacklist();
        entry.setJti(jti);
        entry.setExpiresAt(expiresAt);
        repository.save(entry);

        // Redis에 TTL 설정하여 저장
        long ttlSeconds = (expiresAtDate.getTime() - System.currentTimeMillis()) / 1000;
        if (ttlSeconds > 0) {
            redisTemplate.opsForValue().set("blacklist:" + jti, "blacklisted", ttlSeconds, TimeUnit.SECONDS);
        }
    }

    /**
     * 토큰이 블랙리스트에 존재하는지 확인
     */
    @Transactional(readOnly = true)
    public boolean isBlacklisted(String jti) {
        return repository.findByJti(jti).isPresent();
    }

    /**
     * 정리: 만료된 블랙리스트 항목 삭제 (스케줄러에서 주기적으로 호출)
     */
    @Transactional
    public void purgeExpired() {
        repository.deleteByExpiresAtBefore(LocalDateTime.now());
    }
}