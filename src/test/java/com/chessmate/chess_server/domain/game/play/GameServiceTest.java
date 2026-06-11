package com.chessmate.chess_server.domain.game.play;

import com.chessmate.chess_server.domain.analysis.StockfishService;
import com.chessmate.chess_server.domain.game.common.PlayerColor;
import com.chessmate.chess_server.domain.game.common.ResultReason;
import com.chessmate.chess_server.domain.game.play.dto.MoveRequest;
import com.chessmate.chess_server.domain.game.record.GameRecordService;
import com.chessmate.chess_server.domain.user.User;
import com.chessmate.chess_server.domain.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class GameServiceTest {

    public static final String TEST_GAME_ID = "test-game-id";
    public static final String WHITE_EMAIL = "white@test.com";
    public static final String BLACK_EMAIL = "black@test.com";

    private GameService gameService;
    private GameStateService gameStateService;
    private SimpMessagingTemplate messagingTemplate;
    private UserRepository userRepository;
    private TimerService timerService;
    private GameRecordService gameRecordService;
    private StockfishService stockfishService;

    @BeforeEach
    void beforeEach() {
        gameStateService = mock(GameStateService.class);
        messagingTemplate = mock(SimpMessagingTemplate.class);
        userRepository = mock(UserRepository.class);
        timerService = mock(TimerService.class);
        gameRecordService = mock(GameRecordService.class);
        stockfishService = mock(StockfishService.class);

        gameService = new GameService(gameStateService, messagingTemplate, userRepository,
                 timerService, gameRecordService, stockfishService);
    }

    @Test
    @DisplayName("정상적인 수 입력 시 상태 업데이트")
    void move() {
        GameState gameState = GameState.create(
                TEST_GAME_ID, WHITE_EMAIL, BLACK_EMAIL, 60000L);
        when(gameStateService.find(TEST_GAME_ID)).thenReturn(gameState);

        MoveRequest request = new MoveRequest();
        request.setFrom("e2");
        request.setTo("e4");

        gameService.move(TEST_GAME_ID, WHITE_EMAIL, request);

        assertThat(gameState.getTurn()).isEqualTo(PlayerColor.BLACK);
        assertThat(gameState.getMoves()).hasSize(1);
        verify(gameStateService, times(1)).update(gameState);
        verify(messagingTemplate, times(1)).convertAndSend(
                (String) eq("/topic/game/test-game-id"), (Object) any()
        );
    }

    @Test
    @DisplayName("유효하지 않은 수 입력 시 예외 발생")
    void move_invalid() {
        GameState gameState = GameState.create(
                TEST_GAME_ID, WHITE_EMAIL, BLACK_EMAIL, 60000L);
        when(gameStateService.find(TEST_GAME_ID)).thenReturn(gameState);

        MoveRequest request = new MoveRequest();
        request.setFrom("e2");
        request.setTo("e5");    // 불가능한 수

        assertThatThrownBy(() -> gameService.move(TEST_GAME_ID, WHITE_EMAIL, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("유효하지 않은 수입니다.");
    }

    @Test
    @DisplayName("차례가 아닐 때 예외 발생")
    void move_notYourTurn() {
        GameState gameState = GameState.create(
                TEST_GAME_ID, WHITE_EMAIL, BLACK_EMAIL, 60000L);
        when(gameStateService.find(TEST_GAME_ID)).thenReturn(gameState);

        MoveRequest request = new MoveRequest();
        request.setFrom("e7");
        request.setTo("e7");

        assertThatThrownBy(() -> gameService.move(TEST_GAME_ID, BLACK_EMAIL, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("현재 차례가 아닙니다.");
    }

    @Test
    @DisplayName("게임 종료 처리")
    void finishGame() {
        GameState gameState = GameState.create(
                TEST_GAME_ID, WHITE_EMAIL, BLACK_EMAIL, 60000L);

        User whiteUser = new User(WHITE_EMAIL, "password", "white");
        User blackUser = new User(BLACK_EMAIL, "password", "black");

        when(userRepository.findByEmail(WHITE_EMAIL)).thenReturn(Optional.of(whiteUser));
        when(userRepository.findByEmail(BLACK_EMAIL)).thenReturn(Optional.of(blackUser));
        doNothing().when(gameRecordService).save(
                any(), any(), any(), any(), any()
        );

        gameService.finishGame(TEST_GAME_ID, gameState, PlayerColor.WHITE, ResultReason.RESIGNATION);

        verify(gameStateService, times(1)).delete(TEST_GAME_ID);
        verify(timerService, times(1)).stop(TEST_GAME_ID);
        verify(messagingTemplate, times(1)).convertAndSend(
                (String) eq("/topic/game/test-game-id"), (Object) any()
        );
    }
}