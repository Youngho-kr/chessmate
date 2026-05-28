package com.chessmate.chess_server.domain.game.play.dto;

import lombok.Getter;

@Getter
public class GameOverResponse {

    private final String type = "GAME_OVER";
    private final String result;
    private final String resultReason;
    private final int whiteRatingChange;
    private final int blackRatingChange;

    public GameOverResponse(String result, String resultReason,
                            int whiteRatingChange, int blackRatingChange) {
        this.result = result;
        this.resultReason = resultReason;
        this.whiteRatingChange = whiteRatingChange;
        this.blackRatingChange = blackRatingChange;
    }
}
