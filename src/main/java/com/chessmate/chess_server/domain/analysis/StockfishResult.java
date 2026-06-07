package com.chessmate.chess_server.domain.analysis;

import com.github.bhlangonijr.chesslib.move.Move;
import lombok.Getter;

@Getter
public class StockfishResult {

    private final int evalScore;
    private final String bestMove;

    public StockfishResult(int evalScore, String bestMove) {
        this.evalScore = evalScore;
        this.bestMove = bestMove;
    }
}
