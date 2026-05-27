package com.chessmate.chess_server.domain.game.play;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.util.concurrent.TimeUnit;

@Service
public class GameStateService {

    private static final String KEY_PREFIX = "game:";
    private static final long GAME_TTL = 60 * 60 * 24; // 24시간

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    public GameStateService(RedisTemplate<String, String> redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public void save(GameState gameState) {
        try {
            String key = KEY_PREFIX + gameState.getGameId();
            String value = objectMapper.writeValueAsString(gameState);
            redisTemplate.opsForValue().set(key, value, GAME_TTL, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new RuntimeException("게임 상태 저장 실패", e);
        }
    }

    public GameState find(String gameId) {
        try {
            String key = KEY_PREFIX + gameId;
            String value = redisTemplate.opsForValue().get(key);
            if (value == null) return null;
            return objectMapper.readValue(value, GameState.class);
        } catch (Exception e) {
            throw new RuntimeException("게임 상태 조회 실패", e);
        }
    }

    public void update(GameState gameState) {
        save(gameState);
    }

    public void delete(String gameId) {
        redisTemplate.delete(KEY_PREFIX + gameId);
    }
}
