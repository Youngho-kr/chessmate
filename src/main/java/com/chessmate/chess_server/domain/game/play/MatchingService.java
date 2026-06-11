package com.chessmate.chess_server.domain.game.play;

import com.chessmate.chess_server.domain.game.common.PlayerColor;
import com.chessmate.chess_server.domain.game.play.dto.MatchResponse;
import com.chessmate.chess_server.domain.user.User;
import com.chessmate.chess_server.domain.user.UserRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

@Service
public class MatchingService {

    private static final int DEFAULT_TIME_LIMIT = 60000; // 10분
    private final ConcurrentLinkedQueue<String> waitingQueue = new ConcurrentLinkedQueue<>();

    private final SimpMessagingTemplate messagingTemplate;
    private final UserRepository userRepository;
    private final GameStateService gameStateService;

    public MatchingService(SimpMessagingTemplate messagingTemplate,
                           UserRepository userRepository,
                           GameStateService gameStateService) {
        this.messagingTemplate = messagingTemplate;
        this.userRepository = userRepository;
        this.gameStateService = gameStateService;
    }

    public void join(String playerEmail) {
        if (waitingQueue.contains(playerEmail)) return;

        waitingQueue.add(playerEmail);

        if (waitingQueue.size() >= 2) {
            String whiteId = waitingQueue.poll();
            String blackId = waitingQueue.poll();
            createGame(whiteId, blackId);
        }
    }

    public void cancel(String email) {
        waitingQueue.remove(email);
    }

    private void createGame(String whiteEmail, String blackEmail) {
        String gameId = UUID.randomUUID().toString();
        GameState gameState = GameState.create(gameId, whiteEmail, blackEmail, DEFAULT_TIME_LIMIT);
        gameStateService.save(gameState);

        String whiteNickname = resolveNickname(whiteEmail);
        String blackNickname = resolveNickname(blackEmail);

        messagingTemplate.convertAndSendToUser(
                whiteEmail, "/queue/match",
                new MatchResponse(gameId, PlayerColor.WHITE, blackNickname, (int)DEFAULT_TIME_LIMIT)
        );

        messagingTemplate.convertAndSendToUser(
                blackEmail, "/queue/match",
                new MatchResponse(gameId, PlayerColor.BLACK, whiteNickname, (int)DEFAULT_TIME_LIMIT)
        );
    }

    /**
     * 사용자의 닉네임을 반환한다.
     * 로그인 사용자는 DB에서 조회, 게스트는 "Guest"로 표시한다.
     */
    private String resolveNickname(String playerEmail) {
        if (playerEmail.startsWith("guest:")) return "Guest";
        return userRepository.findByEmail(playerEmail)
                .map(User::getNickname)
                .orElse("Unknown");
    }
}
