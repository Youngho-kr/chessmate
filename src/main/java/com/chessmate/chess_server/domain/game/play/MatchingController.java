package com.chessmate.chess_server.domain.game.play;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
public class MatchingController {

    private final MatchingService matchingService;

    public MatchingController(MatchingService matchingService) {
        this.matchingService = matchingService;
    }

    @MessageMapping("/match/join")
    public void join(Principal principal) {
        matchingService.join(principal.getName());
    }

    @MessageMapping("/match/cancel")
    public void cancel(Principal principal) {
        matchingService.cancel(principal.getName());
    }
}
