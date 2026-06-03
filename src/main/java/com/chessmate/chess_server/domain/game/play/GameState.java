package com.chessmate.chess_server.domain.game.play;

import com.chessmate.chess_server.domain.game.common.PlayerColor;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class GameState {

    private static final String DEFAULT_FEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";

    private String gameId;
    private String whiteEmail;
    private String blackEmail;
    private String fen;
    private PlayerColor turn;
    private long whiteTimeLeftMs;
    private long blackTimeLeftMs;
    private List<String> moves;
    private String drawOfferFrom;

    public GameState() {
        this.moves = new ArrayList<>();
    }

    public static GameState create(String gameId, String whiteEmail, String blackEmail, long timeLimit) {
        GameState state = new GameState();
        state.gameId = gameId;
        state.whiteEmail = whiteEmail;
        state.blackEmail = blackEmail;
        state.fen = DEFAULT_FEN;
        state.turn = PlayerColor.WHITE;
        state.whiteTimeLeftMs = timeLimit;
        state.blackTimeLeftMs = timeLimit;
        state.drawOfferFrom = null;
        return state;
    }
}
