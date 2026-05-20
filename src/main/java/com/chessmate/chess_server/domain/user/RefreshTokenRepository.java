package com.chessmate.chess_server.domain.user;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.concurrent.TimeUnit;

@Repository
public class RefreshTokenRepository {

    private static final String KEY_PREFIX = "refresh:";
    private final RedisTemplate<String, String> redisTemplate;

    public RefreshTokenRepository(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void save(String email, String refreshToken, long expiration) {
        redisTemplate.opsForValue().set(
                KEY_PREFIX + email,
                refreshToken,
                expiration,
                TimeUnit.MILLISECONDS
        );
    }

    public String find(String email) {
        return redisTemplate.opsForValue().get(KEY_PREFIX + email);
    }

    public void delete(String email) {
        redisTemplate.delete(KEY_PREFIX + email);
    }
}
