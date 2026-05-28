package com.chessmate.chess_server.domain.game.record;

import com.chessmate.chess_server.domain.game.common.GameType;
import com.chessmate.chess_server.domain.game.common.ResultReason;
import com.chessmate.chess_server.domain.user.User;
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
                      int whiteChange, int blackChange) {
        whiteUser.updateEloRating(whiteUser.getEloRating() + whiteChange);
        blackUser.updateEloRating(blackUser.getEloRating() + blackChange);

        GameRecord gameRecord = new GameRecord(
                pgn, GameType.PLATFORM, whiteUser, blackUser, resultReason);
        gameRecordRepository.save(gameRecord);
    }
}
