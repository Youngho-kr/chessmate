package com.chessmate.chess_server.domain.game.play;

import com.chessmate.chess_server.domain.game.common.PlayerColor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TimerService {

    private static final long TICK_INTERVAL = 1000L;
    private final Map<String, Long> lastTickMap = new ConcurrentHashMap<>();

    private final GameStateService gameStateService;
    private final GameService gameService;
    private final SimpMessagingTemplate messagingTemplate;

    public TimerService(GameStateService gameStateService, GameService gameService, SimpMessagingTemplate messagingTemplate) {
        this.gameStateService = gameStateService;
        this.gameService = gameService;
        this.messagingTemplate = messagingTemplate;
    }

    public void start(String gameId) {
        lastTickMap.put(gameId, System.currentTimeMillis());
    }

    public void stop(String gameId) {
        lastTickMap.remove(gameId);
    }

    @Scheduled(fixedDelay = 1000)
    public void tick() {
        for (String gameId: new ArrayList<>(lastTickMap.keySet())) {
            long now = System.currentTimeMillis();

            GameState gameState = gameStateService.find(gameId);
            if (gameState == null) {
                lastTickMap.remove(gameId);
                continue;
            }

            long elapsed = now - lastTickMap.get(gameId);
            lastTickMap.put(gameId, now);

            if (gameState.getTurn() == PlayerColor.WHITE) {
                gameState.setWhiteTimeLeftMs(gameState.getWhiteTimeLeftMs() - elapsed);
            } else {
                gameState.setBlackTimeLeftMs(gameState.getBlackTimeLeftMs() - elapsed);
            }

            gameStateService.update(gameState);

            if (gameState.getWhiteTimeLeftMs() <= 0 || gameState.getBlackTimeLeftMs() <= 0) {
                handleTimeout(gameId, gameState);
            }
        }
    }

    private void handleTimeout(String gameId, GameState gameState) {
        stop(gameId);
        gameStateService.delete(gameId);
    }
}
