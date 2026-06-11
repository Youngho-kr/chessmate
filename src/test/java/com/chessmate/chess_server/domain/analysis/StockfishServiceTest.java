package com.chessmate.chess_server.domain.analysis;

import com.chessmate.chess_server.global.config.StockfishConfig;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class StockfishServiceTest {

    static final String INITIAL_POSITION = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";

    @Disabled("Stockfish 설치 환경에서만 진행")
    @Test
    @DisplayName("초기 포지션 평가치")
    void evaluate_initialPosition() {
        StockfishConfig config = new StockfishConfig();
        config.setPath("/opt/homebrew/bin/stockfish");

        StockfishService service = new StockfishService(config);

        StockfishResult result = service.evaluate(INITIAL_POSITION);

        System.out.println("초기 포지션 평가: " + result.getEvalScore());
        assertThat(result.getEvalScore()).isBetween(0, 50);
    }
}