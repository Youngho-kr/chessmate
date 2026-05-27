package com.chessmate.chess_server.domain.game;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class GameState {

    private String gameId;
    private String whiteEmail;
    private String blackEmail;
    private String fen;
    private PlayerColor turn;
    private long whiteTimeLeftMs;
    private long blackTimeLeftMs;
    private List<String> moves;

    public GameState(String gameId, String whiteEmail, String blackEmail, long timeLimit) {
        this.gameId = gameId;
        this.whiteEmail = whiteEmail;
        this.blackEmail = blackEmail;
        this.fen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
        this.turn = PlayerColor.WHITE;
        this.whiteTimeLeftMs = timeLimit;
        this.blackTimeLeftMs = timeLimit;
        this.moves = new ArrayList<>();
    }
}
