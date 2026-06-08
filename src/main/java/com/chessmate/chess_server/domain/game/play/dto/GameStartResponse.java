package com.chessmate.chess_server.domain.game.play.dto;

import com.chessmate.chess_server.domain.game.common.PlayerColor;
import lombok.Getter;

@Getter
public class GameStartResponse {

    private final String type = "GAME_START";
    private final PlayerColor turn;

    public GameStartResponse(PlayerColor turn) {
        this.turn = turn;
    }
}
