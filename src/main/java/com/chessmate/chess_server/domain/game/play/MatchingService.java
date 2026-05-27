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
    private final TimerService timerService;

    public MatchingService(SimpMessagingTemplate messagingTemplate,
                           UserRepository userRepository,
                           GameStateService gameStateService, TimerService timerService) {
        this.messagingTemplate = messagingTemplate;
        this.userRepository = userRepository;
        this.gameStateService = gameStateService;
        this.timerService = timerService;
    }

    public void join(String email) {
        if (waitingQueue.contains(email)) {
            return;
        }

        waitingQueue.add(email);

        if (waitingQueue.size() >= 2) {
            String whiteEmail = waitingQueue.poll();
            String blackEmail = waitingQueue.poll();
            createGame(whiteEmail, blackEmail);
        }
    }

    public void cancel(String email) {
        waitingQueue.remove(email);
    }

    private void createGame(String whiteEmail, String blackEmail) {
        User whiteUser = userRepository.findByEmail(whiteEmail)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));
        User blackUser = userRepository.findByEmail(blackEmail)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        String gameId = UUID.randomUUID().toString();
        GameState gameState = GameState.create(gameId, whiteEmail, blackEmail, DEFAULT_TIME_LIMIT);
        gameStateService.save(gameState);

        messagingTemplate.convertAndSendToUser(
                whiteEmail, "/queue/match",
                new MatchResponse(gameId, PlayerColor.WHITE, blackUser.getNickname(), (int)DEFAULT_TIME_LIMIT)
        );

        messagingTemplate.convertAndSendToUser(
                blackEmail, "/queue/match",
                new MatchResponse(gameId, PlayerColor.BLACK, whiteUser.getNickname(), (int)DEFAULT_TIME_LIMIT)
        );

        timerService.start(gameId);
    }
}
