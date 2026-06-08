package com.chessmate.chess_server.domain.analysis;

import com.chessmate.chess_server.domain.analysis.dto.AnalysisResponse;
import com.chessmate.chess_server.domain.user.User;
import com.chessmate.chess_server.domain.user.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import tools.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api/analysis")
public class AnalysisController {

    private final AnalysisService analysisService;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    public AnalysisController(AnalysisService analysisService,
                              UserRepository userRepository,
                              ObjectMapper objectMapper) {
        this.analysisService = analysisService;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/{gameRecordId}")
    public ResponseEntity<AnalysisResponse> analyze(@PathVariable Long gameRecordId,
                                        @AuthenticationPrincipal String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        Analysis analysis = analysisService.analyze(gameRecordId, user.getId());
        return ResponseEntity.ok(new AnalysisResponse(analysis, objectMapper));
    }
}
