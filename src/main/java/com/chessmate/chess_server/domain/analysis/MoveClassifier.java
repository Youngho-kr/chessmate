package com.chessmate.chess_server.domain.analysis;

import com.chessmate.chess_server.domain.game.common.PlayerColor;
import org.springframework.stereotype.Component;

@Component
public class MoveClassifier {

    public Classification classify(int scoreBefore, int scoreAfter, PlayerColor color) {
        int diff;
        if (color == PlayerColor.WHITE) {
            diff = scoreAfter - scoreBefore;
        } else {
            diff = scoreBefore - scoreAfter;
        }

        if (diff >= 0 ) return Classification.BEST;
        else if (diff >= -20) return Classification.GOOD;
        else if (diff >= -50) return Classification.INACCURACY;
        else if (diff >= -100) return Classification.MISTAKE;
        else return Classification.BLUNDER;
    }
}
