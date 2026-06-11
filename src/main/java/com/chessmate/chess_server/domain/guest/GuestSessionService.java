package com.chessmate.chess_server.domain.guest;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class GuestSessionService {

    private static final String KEY_PREFIX = "guest:";
    private static final long GUEST_TTL = 60 * 60; // 1시간

    private final RedisTemplate<String, String> redisTemplate;

    public GuestSessionService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /*
     * 게스트 세션 발급
     * 서버에서 UUID를 생성하고, Redis에 저장 후 반환
     */
    public String issueSession() {
        String guestId = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set(
                KEY_PREFIX + guestId, "active", GUEST_TTL, TimeUnit.SECONDS
        );
        return guestId;
    }

    /*
     * 게스트 세션 유효성 검증
     * Redis에 해당 guestId가 존재하는지 확인
     */
    public boolean isValid(String guestId) {
        return Boolean.TRUE.equals(
                redisTemplate.hasKey(KEY_PREFIX + guestId)
        );
    }

    /**
     * 로그인 사용자는 email, 게스트는 "guest:{guestId}" 형식의 식별자를 반환한다.
     * 인증 정보가 없거나 유효하지 않은 게스트 세션이라면 empty를 반환한다.
     */
    public Optional<String> resolveId(Principal principal, String guestId) {
        if (principal != null) return Optional.of(principal.getName());
        if (guestId != null && isValid(guestId)) return Optional.of(KEY_PREFIX + guestId);
        return Optional.empty();
    }
}
