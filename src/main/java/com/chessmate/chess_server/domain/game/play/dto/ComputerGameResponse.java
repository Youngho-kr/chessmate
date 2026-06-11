package com.chessmate.chess_server.domain.game.play.dto;

import com.chessmate.chess_server.domain.game.common.PlayerColor;
import lombok.Getter;

@Getter
public class ComputerGameResponse {

    private final String type = "COMPUTER_GAME_START";
    private final String gameId;
    private final PlayerColor playerColor;

    public ComputerGameResponse(String gameId, PlayerColor playerColor) {
        this.gameId = gameId;
        this.playerColor = playerColor;
    }
}
