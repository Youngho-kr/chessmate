package com.chessmate.chess_server.domain.game.record.dto;

import com.chessmate.chess_server.domain.game.common.PlayerColor;
import com.chessmate.chess_server.domain.game.common.ResultReason;
import com.chessmate.chess_server.domain.game.record.GameRecord;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
public class GameSummaryResponse {

    private static final Set<ResultReason> DRAW_REASONS = Set.of(
            ResultReason.STALEMATE,
            ResultReason.DRAW_BY_AGREEMENT,
            ResultReason.THREEFOLD_REPETITION,
            ResultReason.INSUFFICIENT_MATING_MATERIAL,
            ResultReason.FIFTY_MOVE_RULE
    );

    private final Long gameId;
    private final String whitePlayer;
    private final String blackPlayer;
    private final String result;
    private final String resultReason;
    private final LocalDateTime createdAt;

    public GameSummaryResponse(GameRecord gameRecord) {
        this.gameId = gameRecord.getId();

        this.whitePlayer = gameRecord.getWhitePlayer() != null
                ? gameRecord.getWhitePlayer().getNickname() : "Unknown";
        this.blackPlayer = gameRecord.getBlackPlayer() != null
                ? gameRecord.getBlackPlayer().getNickname() : "Unknown";
        this.resultReason = gameRecord.getResultReason() != null
                ? gameRecord.getResultReason().name() : null;
        this.result = determineResult(gameRecord);
        this.createdAt = gameRecord.getCreatedAt();
    }

    private String determineResult(GameRecord gameRecord) {
        if (gameRecord.getResultReason() == null) return "Unknown";
        if (DRAW_REASONS.contains(gameRecord.getResultReason())) return "DRAW";
        return gameRecord.getWinner() == PlayerColor.WHITE ? "WHITE_WIN" : "BLACK_WIN";
    }
}
