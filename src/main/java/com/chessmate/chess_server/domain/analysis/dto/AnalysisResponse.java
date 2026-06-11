package com.chessmate.chess_server.domain.analysis.dto;

import com.chessmate.chess_server.domain.analysis.Analysis;
import lombok.Getter;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;

@Getter
public class AnalysisResponse {

    private final Long id;
    private final double whiteAccuracy;
    private final double blackAccuracy;
    private final Object tree;
    private final LocalDateTime analyzedAt;

    public AnalysisResponse(Analysis analysis, ObjectMapper objectMapper) {
        this.id = analysis.getId();
        this.whiteAccuracy = analysis.getWhiteAccuracy();
        this.blackAccuracy = analysis.getBlackAccuracy();
        this.analyzedAt = analysis.getAnalyzedAt();

        System.out.println("tree raw: " + analysis.getTree().substring(0, 50));

        try {
            this.tree = objectMapper.readValue(analysis.getTree(), Object.class);
        } catch (Exception e) {
            throw new RuntimeException("트리 파싱 실패", e);
        }
    }
}
