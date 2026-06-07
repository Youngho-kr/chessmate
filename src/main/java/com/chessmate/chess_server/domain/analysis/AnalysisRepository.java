package com.chessmate.chess_server.domain.analysis;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AnalysisRepository extends JpaRepository<Analysis, Long> {
    boolean existsByGameRecordIdAndUserId(Long gameRecordId, Long userId);

    Optional<Analysis> findByGameRecordIdAndUserId(Long gameRecordId, Long userId);
}
