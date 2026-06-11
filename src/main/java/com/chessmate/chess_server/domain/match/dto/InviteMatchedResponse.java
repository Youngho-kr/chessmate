package com.chessmate.chess_server.domain.match.dto;

import com.chessmate.chess_server.domain.game.common.PlayerColor;
import lombok.Getter;

@Getter
public class InviteMatchedResponse {

    private final String gameId;
    private final PlayerColor playerColor;

    public InviteMatchedResponse(String gameId, PlayerColor playerColor) {
        this.gameId = gameId;
        this.playerColor = playerColor;
    }
}
