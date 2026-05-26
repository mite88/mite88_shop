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

    private static final String PREFIX = "refresh:";

    private final StringRedisTemplate redisTemplate;

    @Value("${custom.jwt.refresh-expiration}")
    private long refreshExpiration;

    public void save(String username, String refreshToken) {
        redisTemplate.opsForValue().set(PREFIX + username, refreshToken, Duration.ofMillis(refreshExpiration));
    }

    public Optional<String> find(String username) {
        return Optional.ofNullable(redisTemplate.opsForValue().get(PREFIX + username));
    }

    public void delete(String username) {
        redisTemplate.delete(PREFIX + username);
    }

}
