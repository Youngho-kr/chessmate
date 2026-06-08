package com.chessmate.chess_server.domain.analysis;

import com.chessmate.chess_server.domain.game.common.PlayerColor;
import org.springframework.stereotype.Component;

@Component
public class MoveClassifier {

    public Classification classify(int scoreBefore, int scoreAfter, PlayerColor color) {
        int delta = scoreAfter - scoreBefore;

        int playerDelta = color == PlayerColor.WHITE ? delta : -delta;

        if      (playerDelta >= 0 )    return Classification.BEST;
        else if (playerDelta >= -20)   return Classification.GOOD;
        else if (playerDelta >= -50)   return Classification.INACCURACY;
        else if (playerDelta >= -100)  return Classification.MISTAKE;
        else                           return Classification.BLUNDER;
    }
}
