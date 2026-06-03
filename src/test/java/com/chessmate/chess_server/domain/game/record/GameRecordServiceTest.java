package com.chessmate.chess_server.domain.game.record;

import com.chessmate.chess_server.domain.game.common.GameType;
import com.chessmate.chess_server.domain.game.common.PlayerColor;
import com.chessmate.chess_server.domain.game.common.ResultReason;
import com.chessmate.chess_server.domain.game.record.dto.GameDetailResponse;
import com.chessmate.chess_server.domain.game.record.dto.GameSummaryResponse;
import com.chessmate.chess_server.domain.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GameRecordServiceTest {

    private GameRecordService gameRecordService;
    private GameRecordRepository gameRecordRepository;

    private User whiteUser;
    private User blackUser;

    @BeforeEach
    void beforeEach() {
        gameRecordRepository = mock(GameRecordRepository.class);
        gameRecordService = new GameRecordService(gameRecordRepository);

        whiteUser = new User("white@test.com", "password", "white");
        blackUser = new User("black@test.com", "password", "black");
    }

    @Test
    @DisplayName("전적 목록 조회")
    void getGameList() {
        GameRecord gameRecord = new GameRecord(
                "e2e4 e7e5", GameType.PLATFORM,
                whiteUser, blackUser, PlayerColor.WHITE, ResultReason.RESIGNATION
        );

        Page<GameRecord> page = new PageImpl<>(List.of(gameRecord));
        when(gameRecordRepository.findByUserId(1L, PageRequest.of(0, 20)))
                .thenReturn(page);

        Page<GameSummaryResponse> result = gameRecordService.getGameList(1L, PageRequest.of(0, 20));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().getWhitePlayer()).isEqualTo("white");
        assertThat(result.getContent().getFirst().getBlackPlayer()).isEqualTo("black");
        assertThat(result.getContent().getFirst().getResult()).isEqualTo("WHITE_WIN");
    }

    @Test
    @DisplayName("게임 상세 조회")
    void getGameDetail() {
        GameRecord gameRecord = new GameRecord(
                "e2e4 e7e5", GameType.PLATFORM,
                whiteUser, blackUser, PlayerColor.WHITE, ResultReason.RESIGNATION
        );

        Page<GameRecord> page = new PageImpl<>(List.of(gameRecord));
        when(gameRecordRepository.findById(1L)).thenReturn(Optional.of(gameRecord));

        GameDetailResponse result = gameRecordService.getGameDetail(1L);

        assertThat(result.getWhitePlayer()).isEqualTo("white");
        assertThat(result.getBlackPlayer()).isEqualTo("black");
        assertThat(result.getMoves()).containsExactly("e2e4", "e7e5");
    }

    @Test
    @DisplayName("존재하지 않는 게임 조회 시 예외 발생")
    void getGameDetail_notFound() {
        when(gameRecordRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> gameRecordService.getGameDetail(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("존재하지 않는 게임입니다.");
    }
}