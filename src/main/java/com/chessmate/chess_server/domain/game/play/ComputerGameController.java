package com.chessmate.chess_server.domain.game.play;

import com.chessmate.chess_server.domain.game.play.dto.ComputerGameRequest;
import com.chessmate.chess_server.domain.game.play.dto.ComputerGameResponse;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
public class ComputerGameController {

    private final GameService gameService;
    private final SimpMessagingTemplate messagingTemplate;

    public ComputerGameController(GameService gameService,
                                  SimpMessagingTemplate messagingTemplate) {
        this.gameService = gameService;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/game/computer/start")
    public void startComputerGame(@Payload ComputerGameRequest request,
                                  Principal principal) {
        String email = principal.getName();
        ComputerGameResponse response = gameService.startComputerGame(email, request.getSkillLevel());
        messagingTemplate.convertAndSendToUser(email, "/queue/computer", response);

    }
}
