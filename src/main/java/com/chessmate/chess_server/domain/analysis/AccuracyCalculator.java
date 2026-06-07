package com.chessmate.chess_server.domain.analysis;

import com.chessmate.chess_server.domain.game.common.PlayerColor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AccuracyCalculator {

    public double calculate(List<AnalysisNode> nodes, PlayerColor color) {
        List<AnalysisNode> colorNodes = nodes.stream()
                .filter(node -> node.getColor().equals(color))
                .toList();

        if (colorNodes.isEmpty()) return 0.0;

        long goodMoves = colorNodes.stream()
                .filter(node -> node.getClassification() == Classification.BEST
                        || node.getClassification() == Classification.GOOD)
                .count();

        return Math.round((double) goodMoves / colorNodes.size() * 100.0 * 100.0) / 100.0;
    }
}
