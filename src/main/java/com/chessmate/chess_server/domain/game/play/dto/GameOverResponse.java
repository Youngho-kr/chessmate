package com.chessmate.chess_server.domain.game.play.dto;

import lombok.Getter;

@Getter
public class GameOverResponse {

    private final String type = "GAME_OVER";
    private final String result;
    private final String resultReason;

    public GameOverResponse(String result, String resultReason) {
        this.result = result;
        this.resultReason = resultReason;
    }
}
