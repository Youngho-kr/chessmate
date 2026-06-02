package com.chessmate.chess_server.domain.game.record;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface GameRecordRepository extends JpaRepository<GameRecord, Long> {

    @Query("SELECT g from GameRecord g WHERE g.whitePlayer.id = :userId OR g.blackPlayer.id = :userId")
    Page<GameRecord> findByUserId(@Param("userId") Long userId, Pageable pageable);
}
