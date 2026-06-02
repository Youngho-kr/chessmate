package com.chessmate.chess_server.domain.game.play;

import com.chessmate.chess_server.domain.game.common.PlayerColor;
import com.chessmate.chess_server.domain.game.common.ResultReason;
import com.chessmate.chess_server.domain.game.play.dto.*;
import com.chessmate.chess_server.domain.game.record.GameRecordRepository;
import com.chessmate.chess_server.domain.game.record.GameRecordService;
import com.chessmate.chess_server.domain.user.User;
import com.chessmate.chess_server.domain.user.UserRepository;
import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Square;
import com.github.bhlangonijr.chesslib.move.Move;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class GameService {

    private final GameStateService gameStateService;
    private final SimpMessagingTemplate messagingTemplate;
    private final UserRepository userRepository;
    private final EloService eloService;
    private final TimerService timerService;
    private final GameRecordService gameRecordService;

    public GameService(GameStateService gameStateService,
                       SimpMessagingTemplate messagingTemplate,
                       UserRepository userRepository,
                       EloService eloService,
                       TimerService timerService, GameRecordService gameRecordService) {
        this.gameStateService = gameStateService;
        this.messagingTemplate = messagingTemplate;
        this.userRepository = userRepository;
        this.eloService = eloService;
        this.timerService = timerService;
        this.gameRecordService = gameRecordService;
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

        User whiteUser = userRepository.findByEmail(gameState.getWhiteEmail())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));
        User blackUser = userRepository.findByEmail(gameState.getBlackEmail())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        double whiteScore = winner == null ? 0.5 : (winner == PlayerColor.WHITE ? 1.0 : 0.0);
        double blackScore = 1.0 - whiteScore;

        int whiteChange = eloService.calculateChange(
                whiteUser.getEloRating(), blackUser.getEloRating(), whiteScore);
        int blackChange = eloService.calculateChange(
                blackUser.getEloRating(), whiteUser.getEloRating(), blackScore);

        whiteUser.updateEloRating(whiteUser.getEloRating() + whiteChange);
        blackUser.updateEloRating(blackUser.getEloRating() + blackChange);

        String pgn = String.join(" ", gameState.getMoves());
        gameRecordService.save(pgn, resultReason, whiteUser, blackUser, winner, whiteChange, blackChange);

        gameStateService.delete(gameId);

        String result = winner == null ? "DRAW"
                : (winner == PlayerColor.WHITE ? "WHITE_WIN" : "BLACK_WIN");

        messagingTemplate.convertAndSend("/topic/game/" + gameId,
                new GameOverResponse(result, resultReason.name(), whiteChange, blackChange));
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
}