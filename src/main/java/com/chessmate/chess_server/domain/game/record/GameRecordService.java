package com.chessmate.chess_server.domain.game.record;

import com.chessmate.chess_server.domain.game.common.GameType;
import com.chessmate.chess_server.domain.game.common.PlayerColor;
import com.chessmate.chess_server.domain.game.common.ResultReason;
import com.chessmate.chess_server.domain.game.record.dto.GameDetailResponse;
import com.chessmate.chess_server.domain.game.record.dto.GameSummaryResponse;
import com.chessmate.chess_server.domain.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GameRecordService {

    private final GameRecordRepository gameRecordRepository;

    public GameRecordService(GameRecordRepository gameRecordRepository) {
        this.gameRecordRepository = gameRecordRepository;
    }

    @Transactional
    public void save (String pgn, ResultReason resultReason,
                      User whiteUser, User blackUser,
                      PlayerColor winner) {
        GameRecord gameRecord = new GameRecord(
                pgn, GameType.PLATFORM, whiteUser, blackUser, winner, resultReason);
        gameRecordRepository.save(gameRecord);
    }

    public Page<GameSummaryResponse> getGameList(Long userId, Pageable pageable) {
        return gameRecordRepository.findByUserId(userId, pageable)
                .map(GameSummaryResponse::new);
    }

    public GameDetailResponse getGameDetail(Long gameId) {
        GameRecord gameRecord = gameRecordRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게임입니다."));
        return new GameDetailResponse(gameRecord);
    }
}
