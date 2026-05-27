package com.chessmate.chess_server.domain.game.play.dto;

import com.chessmate.chess_server.domain.game.common.PlayerColor;
import lombok.Getter;

@Getter
public class MoveResponse {

    private final String type = "MOVE";
    private final String san;
    private final String fen;
    private final PlayerColor turn;
    private final long whiteTimeLeftMs;
    private final long blackTimeLeftMs;

    public MoveResponse(String san, String fen, PlayerColor turn,
                        long whiteTimeLeftMs, long blackTimeLeftMs) {
        this.san = san;
        this.fen = fen;
        this.turn = turn;
        this.whiteTimeLeftMs = whiteTimeLeftMs;
        this.blackTimeLeftMs = blackTimeLeftMs;
    }
}
