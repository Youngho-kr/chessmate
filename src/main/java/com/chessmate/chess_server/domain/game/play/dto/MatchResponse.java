package com.chessmate.chess_server.domain.game.play.dto;

import com.chessmate.chess_server.domain.game.common.PlayerColor;
import lombok.Getter;

@Getter
public class MatchResponse {

    private final String gameId;
    private final PlayerColor playerColor;
    private final String opponet;
    private final int timeLimit;

    public MatchResponse(String gameId, PlayerColor playerColor, String opponet, int timeLimit) {
        this.gameId = gameId;
        this.playerColor = playerColor;
        this.opponet = opponet;
        this.timeLimit = timeLimit;
    }
}
