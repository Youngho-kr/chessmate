package com.chessmate.chess_server.domain.game.play;

import com.chessmate.chess_server.domain.game.common.PlayerColor;
import com.chessmate.chess_server.domain.game.play.dto.MoveRequest;
import com.chessmate.chess_server.domain.game.play.dto.MoveResponse;
import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.move.Move;
import com.github.bhlangonijr.chesslib.move.MoveList;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class GameService {

    private final GameStateService gameStateService;
    private final SimpMessagingTemplate messagingTemplate;

    public GameService(GameStateService gameStateService,
                       SimpMessagingTemplate messagingTemplate) {
        this.gameStateService = gameStateService;
        this.messagingTemplate = messagingTemplate;
    }

    public void move(String gameId, String email, MoveRequest request) {
        GameState gameState = gameStateService.find(gameId);

        if (gameState == null) {
            throw new IllegalArgumentException("존재하지 않는 게임입니다.");
        }

        validateTurn(gameState, email);

        Board board = new Board();
        board.loadFromFen(gameState.getFen());

        Move move = parseMove(board, request.getSan());

        if (move == null || !board.legalMoves().contains(move)) {
            throw new IllegalArgumentException("유효하지 않은 수입니다.");
        }

        board.doMove(move);
        String newFen = board.getFen();

        updateGameState(gameState, request.getSan(), newFen);
        gameStateService.update(gameState);

        MoveResponse response = new MoveResponse(
                request.getSan(),
                newFen,
                gameState.getTurn(),
                gameState.getWhiteTimeLeftMs(),
                gameState.getBlackTimeLeftMs()
        );

        messagingTemplate.convertAndSend("/topic/game/" + gameId, response);

        if (board.isMated() || board.isStaleMate() || board.isDraw()) {
            handleGameOver(gameId, gameState, board);
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

    private void validateTurn(GameState gameState, String email) {
        boolean isWhiteTurn = gameState.getTurn() == PlayerColor.WHITE;
        boolean isWhitePlayer = email.equals(gameState.getWhiteEmail());

        if (isWhiteTurn != isWhitePlayer) {
            throw new IllegalArgumentException("현재 차례가 아닙니다.");
        }
    }

    private Move parseMove(Board board, String san) {
        try {
            MoveList moveList = new MoveList();
            moveList.loadFromSan(san);
            return moveList.isEmpty() ? null : moveList.get(0);
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

    private void handleGameOver(String gameId, GameState gameState, Board board) {
        gameStateService.delete(gameId);
    }
}