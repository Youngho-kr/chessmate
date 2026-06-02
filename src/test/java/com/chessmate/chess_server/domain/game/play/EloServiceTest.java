package com.chessmate.chess_server.domain.game.play;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EloServiceTest {

    private EloService eloService;

    @BeforeEach
    void beforeEach() {
        eloService = new EloService();
    }

    @Test
    @DisplayName("동일 레이팅에서 승리 시 레이팅 상승")
    void win_sameRating() {
        int change = eloService.calculateChange(1200, 1200, 1.0);
        assertThat(change).isPositive();
    }

    @Test
    @DisplayName("동일 레이팅에서 패배 시 레이팅 감소")
    void lose_sameRating() {
        int change = eloService.calculateChange(1200, 1200, 0.0);
        assertThat(change).isNegative();
    }

    @Test
    @DisplayName("동일 레이팅 무승부 시 레이팅 변동 없음")
    void draw_sameRating() {
        int change = eloService.calculateChange(1200, 1200, 0.5);
        assertThat(change).isZero();
    }

    @Test
    @DisplayName("레이팅이 차이가 클수록 변동폭 커짐")
    void ratingChangeDifference() {
        int changeHighDifference = eloService.calculateChange(1200, 1600, 1.0);
        int changeLowDifference = eloService.calculateChange(1200, 1400, 1.0);

        assertThat(changeHighDifference).isGreaterThan(changeLowDifference);
    }
}