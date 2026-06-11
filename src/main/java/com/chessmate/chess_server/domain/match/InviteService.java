package com.chessmate.chess_server.domain.match;

import com.chessmate.chess_server.domain.game.common.PlayerColor;
import com.chessmate.chess_server.domain.game.play.GameState;
import com.chessmate.chess_server.domain.game.play.GameStateService;
import com.chessmate.chess_server.domain.match.dto.InviteMatchedResponse;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class InviteService {

    private static final String KEY_PREFIX = "invite";
    private static final long INVITE_TTL = 60 * 10; // 10분

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    private final GameStateService gameStateService;
    private final SimpMessagingTemplate messagingTemplate;

    public InviteService(RedisTemplate<String, String> redisTemplate,
                         ObjectMapper objectMapper,
                         GameStateService gameStateService,
                         SimpMessagingTemplate messagingTemplate) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.gameStateService = gameStateService;
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * 초대 코드 생성
     * 방장의 hostId와 gameId를 Redis에 저장
     */
    public String createInvite(String hostId) {
        String code = generateCode();
        String gameId = UUID.randomUUID().toString();
        InviteCode inviteCode = new InviteCode(code, gameId, hostId, System.currentTimeMillis());

        try {
            String value = objectMapper.writeValueAsString(inviteCode);
            redisTemplate.opsForValue().set(KEY_PREFIX + code, value, INVITE_TTL, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new RuntimeException("초대 코드 생성 실패", e);
        }

        return code;
    }

    /**
     * 초대 코드로 게임 참가
     * 유효하지 않은 코드라면 Optional.empty() 반환
     * 성공 시, 게임 상태 생성, 방장에게 WebSocket 알림, gameId 반환
     */
    public Optional<String> joinByCode(String code, String guestId) {
        String value = redisTemplate.opsForValue().get(KEY_PREFIX + code);
        if (value == null) return Optional.empty();

        try {
            InviteCode inviteCode = objectMapper.readValue(value, InviteCode.class);

            GameState gameState = GameState.create(
                    inviteCode.getGameId(),
                    inviteCode.getHostId(),
                    guestId,
                    0L
            );

            redisTemplate.delete(KEY_PREFIX + code);

            messagingTemplate.convertAndSendToUser(
                    inviteCode.getHostId(), "/queue/invite/matched",
                    new InviteMatchedResponse(inviteCode.getGameId(), PlayerColor.WHITE)
            );

            return Optional.of(inviteCode.getGameId());
        } catch (Exception e) {
            throw new RuntimeException("초대 코드 처리 실패", e);
        }
    }

    /**
     * 6자리 숫자 코드 생성
     */
    private String generateCode() {
        return String.format("%06d", (int) (Math.random() * 1_000_000));
    }
}
