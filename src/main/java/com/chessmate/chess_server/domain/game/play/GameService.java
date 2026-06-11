package com.chessmate.chess_server.domain.game.play;

import com.chessmate.chess_server.domain.analysis.StockfishResult;
import com.chessmate.chess_server.domain.analysis.StockfishService;
import com.chessmate.chess_server.domain.game.common.PlayerColor;
import com.chessmate.chess_server.domain.game.common.ResultReason;
import com.chessmate.chess_server.domain.game.play.dto.*;
import com.chessmate.chess_server.domain.game.record.GameRecordService;
import com.chessmate.chess_server.domain.user.User;
import com.chessmate.chess_server.domain.user.UserRepository;
import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Square;
import com.github.bhlangonijr.chesslib.move.Move;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class GameService {

    private final GameStateService gameStateService;
    private final SimpMessagingTemplate messagingTemplate;
    private final UserRepository userRepository;
    private final TimerService timerService;
    private final GameRecordService gameRecordService;
    private final StockfishService stockfishService;

    public GameService(GameStateService gameStateService,
                       SimpMessagingTemplate messagingTemplate,
                       UserRepository userRepository,
                       TimerService timerService,
                       GameRecordService gameRecordService,
                       StockfishService stockfishService) {
        this.gameStateService = gameStateService;
        this.messagingTemplate = messagingTemplate;
        this.userRepository = userRepository;
        this.timerService = timerService;
        this.gameRecordService = gameRecordService;
        this.stockfishService = stockfishService;
    }

    public void move(String gameId, String email, MoveRequest request) {
        GameState gameState = gameStateService.find(gameId);

        if (gameState == null) {
            throw new IllegalArgumentException("존재하지 않는 게임입니다.");
        }

        validateTurn(gameState, email);

        Board board = new Board();
        board.loadFromFen(gameState.getFen());

        Move move = parseMove(board, request.getFrom(), request.getTo());

        if (move == null || !board.legalMoves().contains(move)) {
            throw new IllegalArgumentException("유효하지 않은 수입니다.");
        }

        board.doMove(move);
        String newFen = board.getFen();

        updateGameState(gameState, move.toString(), newFen);
        gameStateService.update(gameState);

        MoveResponse response = new MoveResponse(
                move.toString(),
                newFen,
                gameState.getTurn(),
                gameState.getWhiteTimeLeftMs(),
                gameState.getBlackTimeLeftMs()
        );

        messagingTemplate.convertAndSend("/topic/game/" + gameId, response);

        if (board.isMated()) {
            PlayerColor winner = gameState.getTurn() == PlayerColor.WHITE
                    ? PlayerColor.BLACK
                    : PlayerColor.WHITE;
            finishGame(gameId, gameState, winner, ResultReason.CHECKMATE);
        } else if (board.isStaleMate()) {
            finishGame(gameId, gameState, null, ResultReason.STALEMATE);
        } else if (board.isDraw()) {
            finishGame(gameId, gameState, null, ResultReason.FIFTY_MOVE_RULE);
        }

        if (gameState.isComputerGame() && !board.isMated() && !board.isStaleMate()) {
            makeComputerMove(gameId, gameState);
        }
    }

    public void resign(String gameId, String email) {
        GameState gameState = gameStateService.find(gameId);
        if (gameState == null) throw new IllegalArgumentException("존재하지 않는 게임입니다.");

        PlayerColor winner = email.equals(gameState.getWhiteEmail())
                ? PlayerColor.BLACK
                : PlayerColor.WHITE;

        finishGame(gameId, gameState, winner, ResultReason.RESIGNATION);
    }

    public void draw(String gameId, String email, DrawRequest request) {
        GameState gameState = gameStateService.find(gameId);
        if (gameState == null) throw new IllegalArgumentException("존재하지 않는 게임입니다.");

        switch (request.getAction()) {
            case "OFFER" -> {
                gameState.setDrawOfferFrom(email);
                gameStateService.update(gameState);

                PlayerColor offerColor = email.equals(gameState.getWhiteEmail())
                        ? PlayerColor.WHITE : PlayerColor.BLACK;

                messagingTemplate.convertAndSend("/topic/game/" + gameId,
                        new DrawOfferResponse(offerColor));
            }
            case "ACCEPT" -> {
                gameState.setDrawOfferFrom(null);
                gameStateService.update(gameState);
                finishGame(gameId, gameState, null, ResultReason.DRAW_BY_AGREEMENT);
            }
            case "DECLINE" -> {
                gameState.setDrawOfferFrom(null);
                gameStateService.update(gameState);
                messagingTemplate.convertAndSend("/topic/game/" + gameId,
                        new DrawDeclineResponse());
            }
        }
    }

    public void reconnect(String gameId, String email) {
        GameState gameState = gameStateService.find(gameId);

        if (gameState == null) {
            throw new IllegalArgumentException("존재하지 않는 게임입니다.");
        }

        messagingTemplate.convertAndSendToUser(
                email, "/queue/game/" + gameId, gameState
        );
    }

    public void finishGame(String gameId, GameState gameState,
                           PlayerColor winner, ResultReason resultReason) {
        timerService.stop(gameId);

        User whiteUser = resolveUser(gameState.getWhiteEmail(), gameState, PlayerColor.WHITE);
        User blackUser = resolveUser(gameState.getBlackEmail(), gameState, PlayerColor.BLACK);

        String pgn = String.join(" ", gameState.getMoves());
        gameRecordService.save(pgn, resultReason, whiteUser, blackUser, winner);

        gameStateService.delete(gameId);

        String result = winner == null ? "DRAW"
                : (winner == PlayerColor.WHITE ? "WHITE_WIN" : "BLACK_WIN");

        messagingTemplate.convertAndSend("/topic/game/" + gameId,
                new GameOverResponse(result, resultReason.name()));
    }

    public ComputerGameResponse startComputerGame(String email, int skillLevel) {
        String gameId = UUID.randomUUID().toString();

        // 랜덤으로 유저 색상 결정
        // TODO : 유저가 색상 선택 또는 랜덤 색상할 지 선택 가능하게 수정
        PlayerColor userColor = Math.random() < 0.5 ? PlayerColor.WHITE : PlayerColor.BLACK;
        PlayerColor computerColor = userColor == PlayerColor.WHITE ? PlayerColor.BLACK : PlayerColor.WHITE;

        String stockfishId = "stockfish: " + skillLevel;
        String whitEmail = userColor == PlayerColor.WHITE ? email : stockfishId;
        String blackEmail = userColor == PlayerColor.BLACK ? email : stockfishId;

        GameState gameState = GameState.createComputerGame(
                gameId, whitEmail, blackEmail, skillLevel, computerColor);
        gameStateService.save(gameState);

        return new ComputerGameResponse(gameId, userColor);
    }

    public void makeComputerMove(String gameId, GameState gameState) {
        StockfishResult result = stockfishService.evaluate(
                gameState.getFen(), gameState.getSkillLevel());

        if (result.getBestMove() == null) return;

        String from = result.getBestMove().substring(0, 2);
        String to = result.getBestMove().substring(2, 4);

        Board board = new Board();
        board.loadFromFen(gameState.getFen());
        Move move = parseMove(board, from, to);

        board.doMove(move);
        updateGameState(gameState, move.toString(), board.getFen());
        gameStateService.update(gameState);

        messagingTemplate.convertAndSend("/topic/game/" + gameId,
                new MoveResponse(move.toString(), board.getFen(),
                        gameState.getTurn(),
                        gameState.getWhiteTimeLeftMs(),
                        gameState.getBlackTimeLeftMs()));
    }

    public void ready(String gameId) {
        GameState gameState = gameStateService.find(gameId);
        if (gameState == null) return;
        gameState.setReadyCount(gameState.getReadyCount() + 1);
        gameStateService.update(gameState);

        int requiredCount = gameState.isComputerGame() ? 1 : 2;
        gameState.setReadyCount(gameState.getReadyCount() + 1);
        gameStateService.update(gameState);

        if (gameState.getReadyCount() >= requiredCount) {
            if (!gameState.isComputerGame()) {
                timerService.start(gameId);
            }

            messagingTemplate.convertAndSend("/topic/game/" + gameId,
                    new GameStartResponse(gameState.getTurn()));

            if (gameState.isComputerGame() && gameState.getComputerColor() == PlayerColor.WHITE) {
                makeComputerMove(gameId, gameState);
            }
        }
    }

    private void validateTurn(GameState gameState, String email) {
        boolean isWhiteTurn = gameState.getTurn() == PlayerColor.WHITE;
        boolean isWhitePlayer = email.equals(gameState.getWhiteEmail());

        if (isWhiteTurn != isWhitePlayer) {
            throw new IllegalArgumentException("현재 차례가 아닙니다.");
        }
    }

    private Move parseMove(Board board, String from, String to) {
        try {
            Move move = new Move(
                    Square.fromValue(from.toUpperCase()),
                    Square.fromValue(to.toUpperCase())
            );
            return board.legalMoves().contains(move) ? move : null;
        } catch (Exception e) {
            return null;
        }
    }

    private void updateGameState(GameState gameState, String san, String newFen) {
        gameState.getMoves().add(san);
        gameState.setFen(newFen);
        gameState.setTurn(
                gameState.getTurn() == PlayerColor.WHITE ? PlayerColor.BLACK : PlayerColor.WHITE
        );
    }

    /**
     * 로그인 사용자의 경우 DB에서 조회, 게스트 또는 컴퓨터면 null 반환
     */
    private User resolveUser(String email, GameState gameState, PlayerColor color) {
        if (gameState.isComputerGame() && gameState.getComputerColor() == color) return null;
        if (email.startsWith("guest:")) return null;
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회우너입니다."));
    }
}