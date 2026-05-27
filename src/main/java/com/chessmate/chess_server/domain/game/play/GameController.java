package com.chessmate.chess_server.domain.game.play;

import com.chessmate.chess_server.domain.game.play.dto.MoveRequest;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
public class GameController {

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @MessageMapping("/game/{gameId}/move")
    public void move(@DestinationVariable String gameId,
                     Principal principal,
                     MoveRequest request) {
        gameService.move(gameId, principal.getName(), request);
    }

    @MessageMapping("/game/{gameId}/reconnect")
    public void reconnect(@DestinationVariable String gameId,
                          Principal principal) {
        gameService.reconnect(gameId, principal.getName());
    }
}
