package com.chessmate.chess_server.domain.game.play;

import org.springframework.stereotype.Service;

@Service
public class EloService {

    private static final int K_FACTOR = 32;

    public int calculateChange(int playerRating, int opponentRating, double score) {
        double expectedScore = 1.0 / (1.0 + Math.pow(10, (opponentRating - playerRating) / 400.0));
        return (int) Math.round(K_FACTOR * (score - expectedScore));
    }
}
