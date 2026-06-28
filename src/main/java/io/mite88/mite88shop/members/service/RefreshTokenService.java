package io.mite88.mite88shop.members.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    // Redis 키 형식: "refresh:{username}"
    private static final String PREFIX = "refresh:";

    private final StringRedisTemplate redisTemplate;

    @Value("${custom.jwt.refresh-expiration}")
    private long refreshExpiration;

    /**
     * 리프레시 토큰 및 활성 세션 ID Redis에 저장 - TTL은 토큰 만료 시간과 동일하게 설정
     */
    public void save(String username, String refreshToken, String sessionId) {
        redisTemplate.opsForValue().set(PREFIX + username, refreshToken, Duration.ofMillis(refreshExpiration));
        redisTemplate.opsForValue().set("session:" + username, sessionId, Duration.ofMillis(refreshExpiration));
    }

    /**
     * 저장된 리프레시 토큰 조회
     */
    public Optional<String> find(String username) {
        return Optional.ofNullable(redisTemplate.opsForValue().get(PREFIX + username));
    }

    /**
     * 활성 세션 ID 조회
     */
    public Optional<String> getActiveSessionId(String username) {
        return Optional.ofNullable(redisTemplate.opsForValue().get("session:" + username));
    }

    /**
     * 리프레시 토큰 및 활성 세션 ID 삭제 - 로그아웃 시 호출
     */
    public void delete(String username) {
        redisTemplate.delete(PREFIX + username);
        redisTemplate.delete("session:" + username);
    }

}
