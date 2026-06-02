package com.chessmate.chess_server.domain.game.record.dto;

import com.chessmate.chess_server.domain.game.record.GameRecord;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Getter
public class GameDetailResponse {

    private final Long gameId;
    private final String whitePlayer;
    private final String blackPlayer;
    private final String result;
    private final String resultReason;
    private final List<String> moves;
    private final LocalDateTime createdAt;

    public GameDetailResponse(GameRecord gameRecord) {
        this.gameId = gameRecord.getId();
        this.whitePlayer = gameRecord.getWhitePlayer() != null
                ? gameRecord.getWhitePlayer().getNickname() : "Unknown";
        this.blackPlayer = gameRecord.getBlackPlayer() != null
                ? gameRecord.getBlackPlayer().getNickname() : "Unknown";
        this.result = gameRecord.getWinner() != null
                ? gameRecord.getWinner().name() + "_WIN": "DRAW";
        this.resultReason = gameRecord.getResultReason() != null
                ? gameRecord.getResultReason().name() : null;
        this.moves = gameRecord.getPgn() != null && !gameRecord.getPgn().isBlank()
                ? Arrays.asList(gameRecord.getPgn().split(" "))
                : List.of();
        this.createdAt = gameRecord.getCreatedAt();
    }
}
