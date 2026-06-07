package com.chessmate.chess_server.domain.analysis;

import com.chessmate.chess_server.domain.game.common.PlayerColor;
import lombok.Getter;
import lombok.Setter;

@Getter
public class AnalysisNode {

    private final String id;
    private final String parentId;
    private final int moveNumber;
    private final PlayerColor color;
    private final String san;
    private final String fen;
    private final int evalScore;
    private final String bestMove;
    private final boolean isMainLine;
    @Setter private Classification classification;

    public AnalysisNode(String id, String parentId, int moveNumber,
                        PlayerColor color, String san, String fen,
                        String bestMove, int evalScore, boolean isMainLine) {
        this.id = id;
        this.parentId = parentId;
        this.moveNumber = moveNumber;
        this.color = color;
        this.san = san;
        this.fen = fen;
        this.evalScore = evalScore;
        this.bestMove = bestMove;
        this.isMainLine = isMainLine;
    }
}
